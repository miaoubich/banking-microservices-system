package com.miaoubich.banking.service;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miaoubich.banking.domain.User;
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
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
	}

	@Override
	@Transactional
	@CachePut(value = "users", key = "#id")
	public User updateUser(Long id, User updatedUser) {
		User existingUser = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
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
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
		keycloak.realm(realm).users().delete(user.getKeycloakId());
		userRepository.deleteById(id);
		logger.info("User deleted from DB and Keycloak with id: {}", id);
	}

	@Override
	public List<User> findAllUsers() {
		return userRepository.findAll();
	}
}
