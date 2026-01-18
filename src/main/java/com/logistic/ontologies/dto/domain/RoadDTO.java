package com.logistic.ontologies.dto.domain;

import java.util.List;
import java.util.UUID;

public record RoadDTO(UUID taskId, UUID roadId, String roadType, Integer length, String coatingWear, String weatherId, List<UUID> factors) {
}
