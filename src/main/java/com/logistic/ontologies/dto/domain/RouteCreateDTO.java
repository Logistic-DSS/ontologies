package com.logistic.ontologies.dto.domain;

import java.util.List;
import java.util.UUID;

public record RouteCreateDTO(UUID routeId, List<UUID> roads) {
}
