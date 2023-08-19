package org.georgemalandrakis.archion.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

public class ArchionUser implements Principal {

	private String id;

	private String email;

	private String name;

	//private String role;
	private List<String> roles = new ArrayList<>();

	public ArchionUser() {
	}

	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return this.email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<String>  getRoles() {
		return roles;
	}

	public void addRole(String role) {
		this.roles.add(role);
	}
}
