package com.miaoubich.banking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import feign.RequestInterceptor;

@Configuration
public class FeignClientConfig {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public FeignClientConfig(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    @Bean
    public RequestInterceptor oauth2RequestInterceptor() {
        return requestTemplate -> {
            var authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("keycloak")
                    .principal("fraud-detection-service")
                    .build();
            var authorizedClient = authorizedClientManager.authorize(authorizeRequest);
            if (authorizedClient != null) {
                String token = authorizedClient.getAccessToken().getTokenValue();
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}
