package com.logistic.ontologies.dto.rule;

public record ClassAtom(
        String className,
        Argument variable
) implements Atom {
}