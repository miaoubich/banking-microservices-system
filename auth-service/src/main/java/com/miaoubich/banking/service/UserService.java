package com.miaoubich.banking.service;

import java.util.List;

import com.miaoubich.banking.domain.User;
import com.miaoubich.banking.dto.UpdatePasswordRequest;

public interface UserService {

	User findUserById(String id);
	User updateUser(String id, User updatedUser);
	void deleteUser(String id);
	List<User> findAllUsers();
	void updateUserPasswordByUserId(String userId, UpdatePasswordRequest request);
}
