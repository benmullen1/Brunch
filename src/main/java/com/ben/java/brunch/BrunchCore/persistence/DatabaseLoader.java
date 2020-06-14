/*
 * Copyright Ben Mullen 2016
 * All Rights Reserved
 * 
 */
package com.ben.java.brunch.BrunchCore.persistence;

import com.ben.java.brunch.BrunchCore.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 *
 * @author Ben Mullen
 */
@Component
public class DatabaseLoader implements CommandLineRunner {

	private final ItemRepository repository;

	@Autowired
	public DatabaseLoader(ItemRepository repository) {
		this.repository = repository;
	}

	@Override
	public void run(String... strings) throws Exception {
		this.repository.save(new Item("Eggs", "Some call them Cackleberries", 12));
		this.repository.save(new Item("Donuts", "Hole lot of fun", 6));
		this.repository.save(new Item("Spam", "It can be for breakfast if you fry it", 1));
		//add more loading stuff here
	}
}