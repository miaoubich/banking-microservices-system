package com.miaoubich.banking.service;

import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.miaoubich.banking.domain.User;
import com.miaoubich.banking.dto.AuthResponse;
import com.miaoubich.banking.dto.LoginRequest;
import com.miaoubich.banking.dto.RegisterRequest;
import com.miaoubich.banking.repository.UserRepository;

import jakarta.ws.rs.core.Response;

@Service
public class AuthServiceImpl implements AuthService {

	private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

	private final Keycloak keycloak;
	private final UserRepository userRepository;
	private final RestTemplate restTemplate;

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.server-url}")
	private String serverUrl;

	@Value("${keycloak.client-id}")
	private String clientId;

	@Value("${keycloak.client-secret}")
	private String clientSecret;

	public AuthServiceImpl(Keycloak keycloak, UserRepository userRepository) {
		this.keycloak = keycloak;
		this.userRepository = userRepository;
		this.restTemplate = new RestTemplate();
	}

	@Override
	@Transactional
	public void register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Email already registered: " + request.getEmail());
		}

		/* Create user in Keycloak server */
		//Build Keycloak credentials
		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(request.getPassword());
		credential.setTemporary(false);//user won't be forced to change password on first login

		/* add user to Keycloak */
		//Build Keycloak user object
		UserRepresentation keycloakUser = new UserRepresentation();
		keycloakUser.setUsername(request.getUsername());
		keycloakUser.setEmail(request.getEmail());
		keycloakUser.setFirstName(request.getFirstName());
		keycloakUser.setLastName(request.getLastName());
		keycloakUser.setEnabled(true);//account is active
		keycloakUser.setCredentials(List.of(credential));
		keycloakUser.setEmailVerified(true);//email is verified to avoid email verification step

		UsersResource usersResource = keycloak.realm(realm).users();
		Response response = usersResource.create(keycloakUser);

		if (response.getStatus() != 201) {
			throw new RuntimeException("Failed to create user in Keycloak, status: " + response.getStatus());
		}

		// Extract Keycloak user ID from Location header
		String locationHeader = response.getHeaderString("Location");
		String keycloakId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);

		logger.info("User created in Keycloak with id: {}", keycloakId);

		// Assign role in Keycloak (client level)
		ClientRepresentation clientRepresentation = keycloak.realm(realm).clients()
				.findByClientId(clientId).get(0);
		RoleRepresentation roleRepresentation = keycloak.realm(realm)
				.clients().get(clientRepresentation.getId())
				.roles().get(request.getRole().toKeycloakRole()).toRepresentation();
		
		logger.info("Client ID: {}, Role ID: {}", clientRepresentation.getId(), roleRepresentation.getId());
		
		keycloak.realm(realm).users().get(keycloakId)
				.roles().clientLevel(clientRepresentation.getId())
				.add(List.of(roleRepresentation));

		logger.info("Role {} assigned to user in Keycloak", request.getRole().toKeycloakRole());

		// Save user to DB
		User user = new User(
				request.getFirstName(),
				request.getLastName(),
				request.getUsername(),
				request.getEmail(),
				request.getPhone(),
				keycloakId,
				request.getRole()
		);
		userRepository.save(user);

		logger.info("User saved to DB with email: {}", request.getEmail());
	}

	@Override
	public AuthResponse login(LoginRequest request) {		String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "password");
		body.add("client_id", clientId);
		body.add("client_secret", clientSecret);
		body.add("username", request.getUsername());
		body.add("password", request.getPassword());

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, java.util.Map.class);
			java.util.Map<?, ?> responseBody = response.getBody();
			return new AuthResponse(
					(String) responseBody.get("access_token"),
					(String) responseBody.get("refresh_token"),
					((Number) responseBody.get("expires_in")).longValue()
			);
		} catch (Exception e) {
			logger.error("Login failed for user {}: {}", request.getUsername(), e.getMessage());
			throw new RuntimeException("Invalid email or password");
		}
	}
}
