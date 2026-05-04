package com.miaoubich.banking.controller;

import java.time.LocalDateTime;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miaoubich.banking.domain.AccountStatus;
import com.miaoubich.banking.dto.BalanceRequest;
import com.miaoubich.banking.dto.CreateAccountRequest;
import com.miaoubich.banking.service.AccountService;
import com.miaoubich.banking.service.TransactionService;

/*
 *  Role				Permissions
 * client_super	| 	Full access - can view all accounts, update any account status
 * client_admin	| 	Can create accounts, view own accounts, update account status
 * client_user	|   Can create accounts, view own accounts
 * client_devops|	No access (removed from endpoints)
 */

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;
	private final TransactionService transactionService;

	public AccountController(AccountService accountService, TransactionService transactionService) {
		this.accountService = accountService;
		this.transactionService = transactionService;
	}

	@PostMapping
	@PreAuthorize("hasRole('client_user') or hasRole('client_admin') or hasRole('client_super')")
	public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest request, @AuthenticationPrincipal Jwt jwt) {
		String clientId = jwt.getSubject();
		return ResponseEntity.ok(accountService.createAccount(request, clientId));
	}

	@GetMapping
	@PreAuthorize("hasRole('client_super')")
	public ResponseEntity<?> getAllAccounts() {
		return ResponseEntity.ok(accountService.getAllAccounts());
	}

	@GetMapping("/my")
	@PreAuthorize("hasRole('client_user') or hasRole('client_admin') or hasRole('client_super')")
	public ResponseEntity<?> getMyAccounts(@AuthenticationPrincipal Jwt jwt) {
		String clientId = jwt.getSubject();
		return ResponseEntity.ok(accountService.getAccountsByClientId(clientId));
	}

	@PostMapping("/{accountId}/deposit")
	@PreAuthorize("hasRole('client_user') or hasRole('client_admin') or hasRole('client_super')")
	public ResponseEntity<?> deposit(@PathVariable Long accountId, @RequestBody @Valid BalanceRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(accountService.deposit(accountId, request, jwt.getSubject()));
	}

	@PostMapping("/{accountId}/withdraw")
	@PreAuthorize("hasRole('client_user') or hasRole('client_admin') or hasRole('client_super')")
	public ResponseEntity<?> withdraw(@PathVariable Long accountId, @RequestBody @Valid BalanceRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(accountService.withdraw(accountId, request, jwt.getSubject()));
	}

	@GetMapping("/{accountId}/transactions")
	@PreAuthorize("hasRole('client_user') or hasRole('client_admin') or hasRole('client_super')")
	public ResponseEntity<?> getTransactions(
			@PathVariable Long accountId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		if (from != null && to != null)
			return ResponseEntity.ok(transactionService.getTransactionsByAccountIdAndDateRange(accountId, from, to));
		return ResponseEntity.ok(transactionService.getTransactionsByAccountId(accountId));
	}

	@PatchMapping("/{accountId}/status")
	@PreAuthorize("hasRole('client_admin') or hasRole('client_super')")
	public ResponseEntity<?> updateAccountStatus(@PathVariable Long accountId, @RequestParam AccountStatus status) {
		return ResponseEntity.ok(accountService.updateAccountStatus(accountId, status));
	}
}
