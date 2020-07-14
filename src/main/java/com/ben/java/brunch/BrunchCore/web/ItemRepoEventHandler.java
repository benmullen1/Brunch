/*
 * Copyright Ben Mullen 2016
 * All Rights Reserved
 * 
 */
package com.ben.java.brunch.BrunchCore.web;

import com.ben.java.brunch.BrunchCore.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler(Item.class)
public class ItemRepoEventHandler {

	private final SimpMessagingTemplate websocket;

	private final EntityLinks entityLinks;
	
	private static final String MESSAGE_PREFIX = WebSocketConfiguration.MESSAGE_PREFIX;

	@Autowired
	public ItemRepoEventHandler(SimpMessagingTemplate websocket, EntityLinks entityLinks) {
		this.websocket = websocket;
		this.entityLinks = entityLinks;
	}

	@HandleAfterCreate
	public void newItem(Item item) {
		this.websocket.convertAndSend(
				MESSAGE_PREFIX + "/newItem", getPath(item));
	}

	@HandleAfterDelete
	public void deleteItem(Item item) {
		this.websocket.convertAndSend(
				MESSAGE_PREFIX + "/deleteItem", getPath(item));
	}

	@HandleAfterSave
	public void updateItem(Item item) {
		this.websocket.convertAndSend(
				MESSAGE_PREFIX + "/updateItem", getPath(item));
	}

	/**
	 * Take an {@link Item} and get the URI using Spring Data REST's {@link EntityLinks}.
	 *
	 * @param item
	 */
	private String getPath(Item item) {
		return this.entityLinks.linkForItemResource(item.getClass(),
				item.getId()).toUri().getPath();
	}

}