package com.miaoubich.banking.service;

import java.util.List;

import com.miaoubich.banking.domain.User;
import com.miaoubich.banking.dto.UpdatePasswordRequest;

public interface UserService {

	User findUserById(Long id);
	User updateUser(Long id, User updatedUser);
	void deleteUser(Long id);
	List<User> findAllUsers();
	void updateUserPasswordByUserId(Long userId, UpdatePasswordRequest request);
}
