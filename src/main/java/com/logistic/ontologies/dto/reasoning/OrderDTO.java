package com.logistic.ontologies.dto.reasoning;

import java.util.UUID;

public record OrderDTO(
    UUID taskOnologyId,
    UUID ruleOntologyId,
    Integer orderNumber
) {
}
