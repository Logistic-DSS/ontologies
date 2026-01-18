package com.logistic.ontologies.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import com.logistic.ontologies.dto.TokenValidationResponseDTO;
import com.logistic.ontologies.service.AuthValidationClient;

@Slf4j
@Component
public class ResourceJwtFilter extends OncePerRequestFilter {
    @Autowired
    private AuthValidationClient validationClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing token");
            return;
        }

        try {
            String token = authHeader.substring(7);

            TokenValidationResponseDTO validation =
                validationClient.validateToken(token);
                
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(validation.userId(), null, List.of());

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (HttpClientErrorException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }
}