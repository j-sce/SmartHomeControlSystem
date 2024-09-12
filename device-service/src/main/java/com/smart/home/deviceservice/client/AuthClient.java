package com.smart.home.deviceservice.client;

import com.smart.home.deviceservice.security.TokenValidationRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Log4j2
@Component
public class AuthClient {

    @Value("${auth.api.url}")
    private String authApiUrl;

    private final WebClient webClient;

    @Autowired
    public AuthClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public boolean validateToken(String token) {
        log.info("Token: {}", token);

        URI uri = UriComponentsBuilder.fromUriString(authApiUrl)
                .build()
                .toUri();

        log.debug("Validating token with User Service at: {}", uri.toString());

        try {
            return Boolean.TRUE.equals(webClient.post()
                    .uri(uri)
                    .bodyValue(new TokenValidationRequest(token))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());
        } catch (Exception e) {
            log.error("Error occurred while validating token: {}", e.getMessage());
            return false;
        }
    }

}
