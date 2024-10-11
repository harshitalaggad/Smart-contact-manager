package com.smart.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import java.io.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.helper.Message;
import com.smart.models.ContactDetails;
import com.smart.models.User;
import com.smart.repository.ContactRepository;
import com.smart.repository.UserRepository;

@Controller
@RequestMapping("/user")

public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data to response
	// @model attribute just made the whole add common data method acceseble for all
	// handlers that is they have it al ready they dont even need to call it

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		// principle give u email of user that is unique identifier

		String username = principal.getName();
		System.out.println(username);
		// now we will get all user data using username

		User user = userRepository.getUserByUserName(username);

		// System.out.println(user);
		model.addAttribute("user", user);
	}

	// dashboardhome
	@RequestMapping("/index")
	public String darshboard(Model model, Principal principal) {
		model.addAttribute("title", "Dashboard");

		return "normal/user_dashboard";
	}

	@RequestMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add contact");
		model.addAttribute("contact", new ContactDetails());
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute ContactDetails contact,
			@RequestParam("fileimage") MultipartFile multipartFile, Principal principal, HttpSession httpSession) {

		try {
			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);
			contact.setUser(user);
			// uploading image file to folder
			if (multipartFile.isEmpty()) {
				System.out.println("file is empty");
			} else {

				contact.setImage(multipartFile.getOriginalFilename());
				File file = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());

				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			System.out.println("image uploades");
			user.getContacts().add(contact);
			this.userRepository.save(user);
			// System.out.println(contactDetails);
			// System.out.println("added to database");

			// success message
			httpSession.setAttribute("message", new Message("your contact is added", "success"));
		} catch (Exception e) {
			System.out.println("error" + e.getMessage());
			e.printStackTrace();
			// error message for html page
			httpSession.setAttribute("message", new Message("something went wrong try again!!!!", "danger"));

		}
		return "normal/add_contact_form";
	}

	// per page 5 entries
	// show contacts handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "show-contacts-smart contact manager");
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		Pageable pageable = PageRequest.of(page, 3);
		Page<ContactDetails> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		// will get current page
		model.addAttribute("currentPage", page);
		// will get total page
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}

	// showing particular contact through email
	@RequestMapping("/{cID}/contact")
	public String showContactDetails(@PathVariable("cID") Integer cID, Model model, Principal principal) {

		System.out.println("cid" + cID);

		Optional<ContactDetails> contactoptional = this.contactRepository.findById(cID);
		ContactDetails contactDetails = contactoptional.get();

		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);

		// so that no other person can access details of other user
		if (user.getId() == contactDetails.getUser().getId()) {

			model.addAttribute("contactdetails", contactDetails);
			model.addAttribute("title", contactDetails.getFirstName());

		}

		return "normal/contact_detail";

	}

	// delete handler
	@GetMapping("/delete/{cID}")
	public String deleteContact(@PathVariable("cID") Integer cID, Model model, Principal principal,
			HttpSession httpSession) {
		Optional<ContactDetails> cOptional = this.contactRepository.findById(cID);
		ContactDetails contactDetails = cOptional.get();
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);

		if (user.getId() == contactDetails.getUser().getId()) {
			// as contact is linked with user we have to unlink it refer video 26 in doubt
			contactDetails.setUser(null);
			// delete image from folder too
			/*
			 * String imageName = contactDetails.getImage(); // Assuming image name is
			 * stored in ContactDetails if (imageName != null && !imageName.isEmpty()) {
			 * String imagePath = "static/img" + imageName; File imageFile = new
			 * File(imagePath); if (imageFile.exists()) { imageFile.delete(); } else {
			 * System.out.println("Image file not found: " + imageName); } }
			 */

			this.contactRepository.delete(contactDetails);
			httpSession.setAttribute("message", new Message("Contact Deleted Successfully", "success"));

		}
		return "redirect:/user/show-contacts/0";
	}

	// open update form handler a page where we have form for updating
	@PostMapping("/update-contact/{cID}")
	public String updateform(@PathVariable("cID") Integer cID, Model model) {
		model.addAttribute("title", "update-contact");
		ContactDetails contactDetails = this.contactRepository.findById(cID).get();
		model.addAttribute("contact", contactDetails);

		return "normal/update_form";
	}

	// update contacts handler to procees that is save
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updatecontact(@ModelAttribute ContactDetails contactDetails,
			@RequestParam("fileimage") MultipartFile file, Model m, HttpSession s, Principal p) {

		System.out.println(contactDetails.getcID());
		System.out.println(contactDetails.getFirstName());

		try {
			// old detail

			ContactDetails oldContactDetails = this.contactRepository.findById(contactDetails.getcID()).get();
			if (!file.isEmpty()) {
				// delete old photo

				// not deleting at this moment check later
				File deletefile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deletefile, oldContactDetails.getImage());
				file1.delete();

				// update new photo

				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contactDetails.setImage(file.getOriginalFilename());
			} else {
				contactDetails.setImage(oldContactDetails.getImage());
			}

			User user = this.userRepository.getUserByUserName(p.getName());
			contactDetails.setUser(user);

			this.contactRepository.save(contactDetails);

			s.setAttribute("message", new Message("Your contact is updated", "success"));

		} catch (

		Exception e) {
			// TODO: handle exception
		}

		return "redirect:/user/" + contactDetails.getcID() + "/contact";
	}

	// profile
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		m.addAttribute("title", "profile-SCM");
		return "normal/profile";

	}

	// settings controller
	@GetMapping("/settings")
	public String settings() {

		return "normal/settings";
	}

	// chnge password
	@PostMapping("/change-password")
	public String changePasssword(@RequestParam("oldPassword") String oldpass,
			@RequestParam("newPassword") String newpass, Principal p, BCryptPasswordEncoder b, HttpSession s) {

		System.out.println(oldpass);
		System.out.println(newpass);

		String username = p.getName();

		User user = userRepository.getUserByUserName(username);
		if (this.bCryptPasswordEncoder.matches(oldpass, user.getPassword())) {

			user.setPassword(this.bCryptPasswordEncoder.encode(newpass));
			this.userRepository.save(user);
			s.setAttribute("message", new Message("Your password is successfully changed", "success"));
		} else {
			s.setAttribute("message", new Message("please enter correct old password ", "danger"));
			return "redirect:/user/settings";

		}

		return "redirect:/user/index";

	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
