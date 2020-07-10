/*
 * Copyright Ben Mullen 2016
 * All Rights Reserved
 * 
 */
package com.ben.java.brunch.BrunchCore.model.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import com.ben.java.brunch.BrunchCore.persistence.BrunchUserRepository;
import org.springframework.security.core.userdetails.User;

/**
 *
 * @author Ben Mullen
 */
@Component
public class BrunchUserDetailsService implements UserDetailsService {

	private final BrunchUserRepository repository;

	@Autowired
	public BrunchUserDetailsService(BrunchUserRepository repository) {
		this.repository = repository;
	}

	@Override
	public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
		BrunchUser user = this.repository.findByName(name);
		return new User(user.getName(), user.getPassword(),
				AuthorityUtils.createAuthorityList(user.getRoles()));
	}

}
