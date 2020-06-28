/*
 * Copyright Ben Mullen 2016
 * All Rights Reserved
 * 
 */
package com.ben.java.brunch.BrunchCore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 *
 * @author Ben Mullen
 */
@Entity
public class Item implements Serializable {

    @Id @GeneratedValue 
    private Long id;
    private @Version @JsonIgnore Long version;
    
    private String name;
    private String description;
    private int quantity = 0;

    protected Item() {

    }
    
    public Item(String name, String description) {
	this.name = name;
	this.description = description;
	this.quantity = 0;
    }
    
    public Item(String name, String description, int quantity) {
	this.name = name;
	this.description = description;
	this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || getClass() != o.getClass()) {
	    return false;
	}
	Item item = (Item) o;
	return Objects.equals(id, item.id)
		&& Objects.equals(name, item.name)
		&& Objects.equals(description, item.description);
    }

    @Override
    public int hashCode() {
	//does not include quantity in hash, we don't want 1 egg being considered unique from 2 eggs
	//I think this is the right thing to do -BSM
	return Objects.hash(id, name, description);
    }

    public Long getId() {
	return id;
    }

    public void setId(Long id) {
	this.id = id;
    }

    /**
     * @return the version
     */
    public Long getVersion() {
	return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(Long version) {
	this.version = version;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }
    
    public int getQuantity() {
	return quantity;
    }

    public void setQuantity(int quantity) {
	this.quantity = quantity;
    }

    @Override
    public String toString() {
	return "Item{"
		+ "id=" + Long.toString(id) + '\''
		+ ", name='" + name + '\''
		+ ", description='" + description + '\''
		+ ", quantity='" + Integer.toString(quantity) + '\''
		+ ", version='" + Long.toString(version) + '\''
		+ '}';
    }
}
