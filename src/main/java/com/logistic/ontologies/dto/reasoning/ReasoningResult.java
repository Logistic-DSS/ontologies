package com.logistic.ontologies.dto.reasoning;

import java.util.List;
import java.util.UUID;

public record ReasoningResult(
        UUID taskOntologyId,
        List<InferenceStep> steps
) {
    
}