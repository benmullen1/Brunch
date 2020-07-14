/*
 * Copyright Ben Mullen 2016
 * All Rights Reserved
 * 
 */
package com.ben.java.brunch.BrunchCore.persistence;

import com.ben.java.brunch.BrunchCore.model.Item;
import com.ben.java.brunch.BrunchCore.model.security.BrunchUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Ben Mullen
 */
@Component
public class DatabaseLoader implements CommandLineRunner {

	private final ItemRepository repository;
	private final BrunchUserRepository userRepository;

	@Autowired
	public DatabaseLoader(ItemRepository repository, BrunchUserRepository userRepository) {
		this.repository = repository;
		this.userRepository = userRepository;
	}

	@Override
	public void run(String... strings) throws Exception {
		this.repository.save(new Item("Eggs", "Some call them Cackleberries", 12));
		this.repository.save(new Item("Donuts", "Hole lot of fun", 6));
		this.repository.save(new Item("Spam", "It can be for breakfast if you fry it", 1));
		//add more loading stuff here
		
		this.userRepository.save(new BrunchUser("dev", "dev", BrunchUser.ROLE_ADMIN, BrunchUser.ROLE_USER));
		
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("dev", "credentials", AuthorityUtils.createAuthorityList(BrunchUser.ROLE_ADMIN, BrunchUser.ROLE_USER)));
		
		SecurityContextHolder.clearContext();
	}
}