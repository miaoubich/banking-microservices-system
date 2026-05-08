package com.miaoubich.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class FraudDetectionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudDetectionServiceApplication.class, args);
	}

	@Bean
	public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository) {
		var authorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
		var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
		authorizedClientManager.setAuthorizedClientProvider(
				OAuth2AuthorizedClientProviderBuilder.builder()
						.clientCredentials()
						.build());
		return authorizedClientManager;
	}
}
