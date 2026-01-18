package com.logistic.ontologies.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.logistic.ontologies.dto.TokenValidationResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthValidationClient {
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String authServiceUri;

    @Autowired
    private RestTemplate restTemplate;

    public TokenValidationResponseDTO validateToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TokenValidationResponseDTO> response = restTemplate.exchange(
            authServiceUri + "/api/auth/validate",
            HttpMethod.POST,
            entity,
            TokenValidationResponseDTO.class
        );

        return response.getBody();
    }
}