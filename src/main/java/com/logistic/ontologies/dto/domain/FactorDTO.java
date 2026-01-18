package com.logistic.ontologies.dto.domain;

import java.util.UUID;

public record FactorDTO(UUID taskId, UUID factorId, String factorName, Integer factorValue) {
}
