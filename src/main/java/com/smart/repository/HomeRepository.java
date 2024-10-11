package com.smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.models.User;

public interface HomeRepository extends JpaRepository<User, Integer> {

}
