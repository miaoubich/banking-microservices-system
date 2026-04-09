package com.miaoubich.banking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miaoubich.banking.domain.User;
import com.miaoubich.banking.dto.UpdatePasswordRequest;
import com.miaoubich.banking.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	@PreAuthorize("hasRole('client_super')")
	public ResponseEntity<List<User>> getAllUsers() {
		return ResponseEntity.ok(userService.findAllUsers());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('client_super') or hasRole('client_admin')")
	public ResponseEntity<User> findUserById(@PathVariable Long id) {
		return ResponseEntity.ok(userService.findUserById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('client_super') or hasRole('client_admin')")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
		return ResponseEntity.ok(userService.updateUser(id, updatedUser));
	}
	
	@PatchMapping("/{id}/password")
	@PreAuthorize("hasRole('client_super') or hasRole('client_admin')")
	public ResponseEntity<String> updateUserPassword(@PathVariable Long id, @RequestBody UpdatePasswordRequest request) {
		userService.updateUserPasswordByUserId(id, request);
		return ResponseEntity.ok("Password updated successfully");
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('client_super')")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}
}
