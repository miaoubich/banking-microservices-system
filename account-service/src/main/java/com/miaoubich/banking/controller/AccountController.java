package com.miaoubich.banking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miaoubich.banking.dto.CreateAccountRequest;
import com.miaoubich.banking.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping
	public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest request){//, @AuthenticationPrincipal Jwt jwt) {
		//Long clientId = Long.parseLong(jwt.getSubject());
		Long clientId = 1L; // TODO: replace with jwt.getSubject() when auth-service is ready;
		return ResponseEntity.ok(accountService.createAccount(request, clientId));
	}

}
