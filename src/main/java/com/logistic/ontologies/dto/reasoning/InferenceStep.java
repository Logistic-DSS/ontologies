package com.logistic.ontologies.dto.reasoning;

import java.util.List;
import java.util.UUID;

public record InferenceStep(
        UUID ruleOntologyId,
        List<String> inferredFacts
) {}
