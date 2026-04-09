package com.miaoubich.banking.service;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miaoubich.banking.domain.User;
import com.miaoubich.banking.dto.UpdatePasswordRequest;
import com.miaoubich.banking.exception.UserNotFoundException;
import com.miaoubich.banking.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
	
	private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

	private final Keycloak keycloak;
	private final UserRepository userRepository;

	@Value("${keycloak.realm}")
	private String realm;

	public UserServiceImpl(Keycloak keycloak, UserRepository userRepository) {
		this.keycloak = keycloak;
		this.userRepository = userRepository;
	}

	@Override
	@Cacheable(value = "users", key = "#id")
	public User findUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));
	}

	@Override
	@Transactional
	@CachePut(value = "users", key = "#id")
	public User updateUser(Long id, User updatedUser) {
		User existingUser = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));
		existingUser.setFirstName(updatedUser.getFirstName());
		existingUser.setLastName(updatedUser.getLastName());
		existingUser.setPhone(updatedUser.getPhone());
		return userRepository.save(existingUser);
	}

	@Override
	@Transactional
	@CacheEvict(value = "users", key = "#id")
	public void deleteUser(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));
		keycloak.realm(realm).users().delete(user.getKeycloakId());
		userRepository.deleteById(id);
		logger.info("User deleted from DB and Keyd: {}", id);
	}

	@Override
	public List<User> findAllUsers() {
		return userRepository.findAll();
	}

	@Override
	public void updateUserPasswordByUserId(Long userId, UpdatePasswordRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(request.getNewPassword());
		credential.setTemporary(false);

		keycloak.realm(realm).users().get(user.getKeycloakId()).resetPassword(credential);
		logger.info("Password updated in Keycloak for user id: {}", userId);
	}
}
