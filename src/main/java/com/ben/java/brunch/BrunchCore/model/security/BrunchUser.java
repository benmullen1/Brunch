/*
 * Copyright Ben Mullen 2016
 * All Rights Reserved
 * 
 */
package com.ben.java.brunch.BrunchCore.model.security;

import java.util.Arrays;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import javax.persistence.ManyToMany;
import javax.persistence.Version;



/**
 *
 * @author Ben Mullen
 */
@Entity
public class BrunchUser {

	public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
	public static final String ROLE_MANAGER = "manager";
	public static final String ROLE_ADMIN = "admin";
	public static final String ROLE_USER = "user";

	private @Id @GeneratedValue Long id;
	private String name;
	private @JsonIgnore String password;
	private String[] roles;
	private @ManyToMany Set<BrunchUser> managingUsers;
	private @ManyToMany Set<BrunchUser> subordinateUsers;
	private @ManyToMany Set<BrunchUser> delegateUsers;
	private @Version @JsonIgnore Long version;
	
	public void setPassword(String password) {
		this.password = PASSWORD_ENCODER.encode(password);
	}	

	protected BrunchUser() {}

	public BrunchUser(String name, String password, String... roles) {
		//cannot set managers, subordinates or delegates through constructor, must do that separately
		this.name = name;
		this.setPassword(password);
		this.roles = roles;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BrunchUser manager = (BrunchUser) o;
		return Objects.equals(id, manager.id) &&
			Objects.equals(name, manager.name) &&
			Objects.equals(password, manager.password) &&
			Arrays.equals(roles, manager.roles);
	}

	@Override
	public int hashCode() {

		int result = Objects.hash(id, name, password);
		result = 31 * result + Arrays.hashCode(roles);
		return result;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		return "User{" +
			"id=" + id +
			", name='" + name + '\'' +
			", roles=" + Arrays.toString(roles) +
			'}';
	}

    /**
     * @return the managingUsers
     */
    public Set<BrunchUser> getManagingUsers() {
	return managingUsers;
    }

    /**
     * @param managingUsers the managingUsers to set
     */
    public void setManagingUsers(Set<BrunchUser> managingUsers) {
	this.managingUsers = managingUsers;
    }

    /**
     * @return the subordinateUsers
     */
    public Set<BrunchUser> getSubordinateUsers() {
	//might need to do some checking to see if the tree is 'clean', but for now just allow the entire tree to be replaced at once
	//this comment applies to both sub and manager collections. see addManager and addSubordinate for more notes
	return subordinateUsers;
    }

    /**
     * @param subordinateUsers the subordinateUsers to set
     */
    public void setSubordinateUsers(Set<BrunchUser> subordinateUsers) {
	this.subordinateUsers = subordinateUsers;
    }

    /**
     * @return the delegateUsers
     */
    public Set<BrunchUser> getDelegateUsers() {
	return delegateUsers;
    }

    /**
     * @param delegateUsers the delegateUsers to set
     */
    public void setDelegateUsers(Set<BrunchUser> delegateUsers) {
	this.delegateUsers = delegateUsers;
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
    
    public boolean addManager(BrunchUser user){
	//we don't allow a user to be both a manager and subordinate
	//the user record should have their own direct report and their own direct manager only, and 
	//it is assumed this will be a hierarchy and not a graph/circle
	//we do however, add a subordinate to the manager's subordinate lists while adding the manager (and vv)
	if (this.subordinateUsers.contains(user)){
	    return false;
	}
	user.addSubordinate(this);
	return this.managingUsers.add(user);
    }
    
    public boolean addSubordinate(BrunchUser user){
	//disallow being both manager and subordinate
	if (this.managingUsers.contains(user)){
	    return false;
	}
	user.addManager(this);
	return this.subordinateUsers.add(user);
    }
    
    public boolean addDelegate(BrunchUser user){
	return this.delegateUsers.add(user);
    }
    
    public boolean removeManager(BrunchUser user){
	boolean managerRemoved = false;
	if (this.managingUsers.contains(user)){
	    //remove this first so that when we remove the subordinate record, it doesn't inf loop
	    //as the removeSubordinate below will attempt to remove the manager
	    this.managingUsers.remove(user);
	    managerRemoved = true;
	    if (user.getSubordinateUsers().contains(this)){		
		user.removeSubordinate(this);
	    }
	}
	return managerRemoved;
    }
    
    public boolean removeSubordinate(BrunchUser user){
	boolean subordinateRemoved = false;
	if (this.subordinateUsers.contains(user)){
	    this.subordinateUsers.remove(user);
	    subordinateRemoved = true;
	    //if this were called by removeManager, when we do this check it should come up dry
	    if (user.getManagingUsers().contains(this)){
		user.removeManager(this);
	    }
	}
	return subordinateRemoved;
    }
    
}
