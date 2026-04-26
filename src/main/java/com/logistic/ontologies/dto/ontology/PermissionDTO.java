package com.logistic.ontologies.dto.ontology;

import java.util.UUID;

import com.logistic.ontologies.model.OntologyType;
import com.logistic.ontologies.model.UserRole;

public record PermissionDTO(
    OntologyType ontologyType,
    UUID ontologyId,
    UUID userId,
    UserRole userRole
) {
}
