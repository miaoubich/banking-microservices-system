package com.miaoubich.banking.service;

import java.util.List;

import com.miaoubich.banking.domain.User;
import com.miaoubich.banking.dto.UpdatePasswordRequest;

public interface UserService {

	User findUserById(long id);
	User updateUser(long id, User updatedUser);
	void deleteUser(long id);
	List<User> findAllUsers();
	void updateUserPasswordByUserId(long userId, UpdatePasswordRequest request);
}
