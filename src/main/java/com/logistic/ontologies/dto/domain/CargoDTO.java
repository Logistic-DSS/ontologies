package com.logistic.ontologies.dto.domain;

import java.util.UUID;

public record CargoDTO(UUID taskId, UUID cargoId, String name, Integer weight) {
}
