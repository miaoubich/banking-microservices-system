package com.miaoubich.banking.service;

import com.miaoubich.banking.dto.AuthResponse;
import com.miaoubich.banking.dto.LoginRequest;
import com.miaoubich.banking.dto.RegisterRequest;

public interface AuthService {

	void register(RegisterRequest request);
	AuthResponse login(LoginRequest request);
}
