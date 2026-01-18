package com.logistic.ontologies.dto.domain;

import java.util.List;
import java.util.UUID;

public record RouteDTO(UUID taskId, UUID routeId, List<UUID> roads) {
}
