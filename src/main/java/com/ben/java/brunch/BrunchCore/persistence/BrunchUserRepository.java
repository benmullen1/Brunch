/*
 * Copyright Ben Mullen 2016
 * All Rights Reserved
 * 
 */
package com.ben.java.brunch.BrunchCore.persistence;

import com.ben.java.brunch.BrunchCore.model.security.BrunchUser;
import java.util.Set;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author Ben Mullen
 */
@RepositoryRestResource(exported = false)
public interface BrunchUserRepository extends PagingAndSortingRepository<BrunchUser, Long> {
	BrunchUser findByName(String name);	
	Set<BrunchUser> getOrganization(BrunchUser user); //should retrieve this user and all direct subordinates, and all grand-subordinates etc.
					      //not sure if this one is feasible
}
