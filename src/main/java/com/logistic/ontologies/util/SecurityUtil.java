package com.logistic.ontologies.util;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public UUID getUserId() {
        return (UUID) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
    }
}
