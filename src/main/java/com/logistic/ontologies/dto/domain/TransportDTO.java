package com.logistic.ontologies.dto.domain;

import java.util.List;
import java.util.UUID;

public record TransportDTO(UUID taskId, UUID transportId, String name, Integer maxSpeed, Integer payload, List<UUID> cargos) {

}
