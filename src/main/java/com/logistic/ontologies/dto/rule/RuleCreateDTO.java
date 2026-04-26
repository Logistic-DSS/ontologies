package com.logistic.ontologies.dto.rule;

import java.util.List;

public record RuleCreateDTO(List<Atom> antecedent, List<Atom> consequent) {
}