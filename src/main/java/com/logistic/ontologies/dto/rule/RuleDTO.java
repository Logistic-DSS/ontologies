package com.logistic.ontologies.dto.rule;

import java.util.List;
import java.util.UUID;

public record RuleDTO(UUID ruleOntologyId, UUID ruleId, List<Atom> antecedent, List<Atom> consequent) {

}