package com.miaoubich.banking.job;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.miaoubich.banking.domain.User;
import com.miaoubich.banking.domain.UserRole;
import com.miaoubich.banking.repository.UserRepository;

import jakarta.ws.rs.core.Response;

@Component
public class UserSyncJob {

	private static final Logger logger = LoggerFactory.getLogger(UserSyncJob.class);

	private final Keycloak keycloak;
	private final UserRepository userRepository;

	@Value("${keycloak.realm}")
	private String realm;

	public UserSyncJob(Keycloak keycloak, UserRepository userRepository) {
		this.keycloak = keycloak;
		this.userRepository = userRepository;
	}

	@Scheduled(fixedRate = 100000) // Run every 1 minutes
	public void syncUsers() {
		logger.info("Starting user synchronization between Database and Keycloak");
		
		try {
			// Get all users from database
			List<User> dbUsers = userRepository.findAll();
			Map<String, User> dbUsersByKeycloakId = dbUsers.stream()
					.collect(Collectors.toMap(User::getKeycloakId, user -> user));

			// Get all users from Keycloak
			UsersResource usersResource = keycloak.realm(realm).users();
			List<UserRepresentation> keycloakUsers = usersResource.list();
			Set<String> keycloakUserIds = keycloakUsers.stream()
					.map(UserRepresentation::getId)
					.collect(Collectors.toSet());

			// Find users in DB but not in Keycloak
			List<User> orphanedDbUsers = dbUsers.stream()
					.filter(user -> !keycloakUserIds.contains(user.getKeycloakId()))
					.toList();

			// Find users in Keycloak but not in DB
			List<UserRepresentation> orphanedKeycloakUsers = keycloakUsers.stream()
					.filter(kcUser -> !dbUsersByKeycloakId.containsKey(kcUser.getId()))
					.toList();

			// Sync orphaned DB users to Keycloak
			int syncedToKeycloak = 0;
			for (User dbUser : orphanedDbUsers) {
				try {
					createUserInKeycloak(dbUser);
					syncedToKeycloak++;
					logger.info("Synced DB user to Keycloak: {} ({})", dbUser.getUsername(), dbUser.getEmail());
				} catch (Exception e) {
					logger.error("Failed to sync DB user {} to Keycloak: {}", dbUser.getUsername(), e.getMessage());
				}
			}

			// Sync orphaned Keycloak users to DB
			int syncedToDb = 0;
			for (UserRepresentation kcUser : orphanedKeycloakUsers) {
				try {
					createUserInDatabase(kcUser);
					syncedToDb++;
					logger.info("Synced Keycloak user to DB: {} ({})", kcUser.getUsername(), kcUser.getEmail());
				} catch (Exception e) {
					logger.error("Failed to sync Keycloak user {} to DB: {}", kcUser.getUsername(), e.getMessage());
				}
			}

			// Log sync results
			logger.info("Sync Summary:");
			logger.info("- Database users: {}", dbUsers.size());
			logger.info("- Keycloak users: {}", keycloakUsers.size());
			logger.info("- Synced DB → Keycloak: {}", syncedToKeycloak);
			logger.info("- Synced Keycloak → DB: {}", syncedToDb);

		} catch (Exception e) {
			logger.error("Error during user synchronization: {}", e.getMessage(), e);
		}
	}

	private void createUserInKeycloak(User dbUser) {
		// Create Keycloak user representation
		UserRepresentation keycloakUser = new UserRepresentation();
		keycloakUser.setUsername(dbUser.getUsername());
		keycloakUser.setEmail(dbUser.getEmail());
		keycloakUser.setFirstName(dbUser.getFirstName());
		keycloakUser.setLastName(dbUser.getLastName());
		keycloakUser.setEnabled(true);
		keycloakUser.setEmailVerified(true);

		// Set temporary password (user should change it)
		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue("TempPassword123!"); // Temporary password
		credential.setTemporary(true); // Force password change on first login
		keycloakUser.setCredentials(List.of(credential));

		// Create user in Keycloak
		UsersResource usersResource = keycloak.realm(realm).users();
		Response response = usersResource.create(keycloakUser);

		if (response.getStatus() != 201) {
			throw new RuntimeException("Failed to create user in Keycloak, status: " + response.getStatus());
		}

		// Extract new Keycloak ID and update DB user
		String locationHeader = response.getHeaderString("Location");
		String newKeycloakId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
		
		// Update DB user with correct Keycloak ID
		dbUser.setKeycloakId(newKeycloakId);
		userRepository.save(dbUser);
	}

	private void createUserInDatabase(UserRepresentation kcUser) {
		// Extract user roles from Keycloak
		UserRole userRole = extractUserRoleFromKeycloak(kcUser.getId());
		
		// Create DB user from Keycloak user
		User dbUser = new User(
				kcUser.getFirstName() != null ? kcUser.getFirstName() : "Unknown",
				kcUser.getLastName() != null ? kcUser.getLastName() : "Unknown",
				kcUser.getUsername(),
				kcUser.getEmail(),
				"000-000-0000", // Default phone
				kcUser.getId(),
				userRole
		);
		
		userRepository.save(dbUser);
	}

	private UserRole extractUserRoleFromKeycloak(String keycloakUserId) {
		try {
			// Get client representation
			var clientRepresentation = keycloak.realm(realm).clients()
					.findByClientId("banking-system").get(0);
			
			// Get user's client-level roles
			var clientRoles = keycloak.realm(realm).users().get(keycloakUserId)
					.roles().clientLevel(clientRepresentation.getId()).listAll();
			
			// Find the highest priority role
			for (var role : clientRoles) {
				switch (role.getName()) {
					case "client_super":
						return UserRole.client_super;
					case "client_admin":
						return UserRole.client_admin;
					case "client_user":
						return UserRole.client_user;
					case "client_devops":
						return UserRole.client_devops;
				}
			}
			
			// Check realm-level roles as fallback
			var realmRoles = keycloak.realm(realm).users().get(keycloakUserId)
					.roles().realmLevel().listAll();
			
			for (var role : realmRoles) {
				switch (role.getName()) {
					case "client_super":
						return UserRole.client_super;
					case "client_admin":
						return UserRole.client_admin;
					case "client_user":
						return UserRole.client_user;
					case "client_devops":
						return UserRole.client_devops;
				}
			}
			
		} catch (Exception e) {
			logger.warn("Failed to extract role for Keycloak user {}: {}", keycloakUserId, e.getMessage());
		}
		
		// Default fallback
		return UserRole.client_user;
	}
}