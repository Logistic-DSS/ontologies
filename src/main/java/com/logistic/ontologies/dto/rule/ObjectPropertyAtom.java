package com.logistic.ontologies.dto.rule;

public record ObjectPropertyAtom(
    String objectPropertyName,
    Argument subject,
    Argument object
) implements Atom {
}
