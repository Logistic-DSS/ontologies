package com.logistic.ontologies.dto.ontology;

import java.util.Date;
import java.util.UUID;

public record OntologyDTO(UUID ontologyId, UUID ownerId, String name, String description, Date createdAt, Date updatedAt) {
}