package com.smart.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.models.ContactDetails;

public interface ContactRepository extends JpaRepository<ContactDetails, Integer> {
//pagination....

	@Query("from ContactDetails as c where c.user.id =:userId")
	//return sublist from list
	//CURRENT PAGE-page
	//contact perpage-5
	public Page<ContactDetails> findContactsByUser(@Param("userId") int user_id, Pageable pageable );

	
	
}
