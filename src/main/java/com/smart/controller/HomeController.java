package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.smart.helper.Message;
import com.smart.models.User;
import com.smart.repository.HomeRepository;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private HomeRepository homeRepository;

	// handler,home page
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "home-smart Contact manager");
		return "home";

	}

	@RequestMapping("/about")
	public String about(Model model) {

		model.addAttribute("title", "about-smart Contact manager");
		return "about";

	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "sign-up-smart Contact manager");
		model.addAttribute("user", new User());
		return "signup";

	}

//here we are not just showing a page but sending the data to database also thats why we use post method
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser( @ModelAttribute("user") User user,Model model,
			HttpSession session) {
		try {

			
			
			
			
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			// saving data in database
			// System.out.println(user);
			User result1 = homeRepository.save(user);

			// System.out.println(result1);
			session.setAttribute("message", new Message("sucessfully Registered", "alert-success"));
			return "signup"; // Redirect to signup page or any other appropriate page

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("something went wrong" + e.getMessage(), "alert-danger");
			return "signup";

		}

	}

	@RequestMapping(value = "/signin")
	public String login(Model model) {
		model.addAttribute("title", "login-smart Contact manager");
		return "login";

	}

}
