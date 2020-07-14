/*
 * Copyright Ben Mullen 2016
 * All Rights Reserved
 * 
 */
package com.ben.java.brunch.BrunchCore.model.security;

import com.ben.java.brunch.BrunchCore.persistence.BrunchUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 *
 * @author Ben Mullen
 */
@Component
@RepositoryEventHandler(BrunchUser.class)
public class BrunchUserEventHandler {

	private final BrunchUserRepository userRepository;

	@Autowired
	public BrunchUserEventHandler(BrunchUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@HandleBeforeCreate
	@HandleBeforeSave
	public void applyUserInformationUsingSecurityContext(BrunchUser newUser) {
		//this event handler will add any new account created by a user as a subordinate
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		BrunchUser currentUser = this.userRepository.findByName(name);
		if (currentUser != null) {
		    newUser.addManager(currentUser);
		} else{
		    if (name != null && !newUser.getName().equals(name)){
			//we have a user who is authenticated but has no user record, and they are trying to create a new user account but 
			//not for their own username. Shenanigans declared
			//we allow the creation event to go through for unauthenticated users because presumably they will not have a username in the session
			//while they are attempting to create an account
			throw new UsernameNotFoundException("Currently Authenticated User " + name + " did not have a user record in the system");
		    }
		    
		}
	}
}