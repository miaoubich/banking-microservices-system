package com.miaoubich.banking.domain;

public enum UserRole {
	client_admin("client_admin"),
	client_user("client_user"),
	client_super("client_super");

	private final String keycloakRoleName;

	UserRole(String keycloakRoleName) {
		this.keycloakRoleName = keycloakRoleName;
	}

	public String toKeycloakRole() {
		return keycloakRoleName;
	}
}
