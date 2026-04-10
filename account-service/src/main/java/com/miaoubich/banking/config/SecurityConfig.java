package com.miaoubich.banking.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/**").permitAll()
				.requestMatchers("/api/accounts/**").hasAnyRole("client_user", "client_admin", "client_super")
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
			);
		return http.build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
		return converter;
	}

	@SuppressWarnings("unchecked")
	private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		Set<GrantedAuthority> authorities = new HashSet<>();

		Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
		if (realmAccess != null)
			((List<String>) realmAccess.getOrDefault("roles", Collections.emptyList())).stream()
					.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
					.forEach(authorities::add);

		Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
		if (resourceAccess != null && resourceAccess.containsKey("banking-system")) {
			Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("banking-system");
			((List<String>) clientAccess.getOrDefault("roles", Collections.emptyList())).stream()
					.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
					.forEach(authorities::add);
		}

		return authorities;
	}
}
