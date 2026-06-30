package com.miaoubich.banking.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.miaoubich.banking.domain.User;

public interface UserRepository extends MongoRepository<User, Long> {

	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
}
