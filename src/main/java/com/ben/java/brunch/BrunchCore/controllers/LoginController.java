/*
 * Copyright Ben Mullen 2016
 * All Rights Reserved
 * 
 */
package com.ben.java.brunch.BrunchCore.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Ben Mullen
 */
@Controller
public class LoginController {

	@RequestMapping(value = "/login")
	public String login() {
		return "login";
	}

}
