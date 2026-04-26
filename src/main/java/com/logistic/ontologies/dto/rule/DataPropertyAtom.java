package com.logistic.ontologies.dto.rule;

public record DataPropertyAtom(
    String dataPropertyName,
    Argument subject,
    Argument value
) implements Atom {
}
