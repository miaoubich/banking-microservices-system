package com.miaoubich.banking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miaoubich.banking.domain.AccountStatus;
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
	
	@PatchMapping("/{accountId}/status")
	public ResponseEntity<?> updateAccountStatus(@PathVariable Long accountId, @RequestParam AccountStatus status) {
		return ResponseEntity.ok(accountService.updateAccountStatus(accountId, status));
	}

}
