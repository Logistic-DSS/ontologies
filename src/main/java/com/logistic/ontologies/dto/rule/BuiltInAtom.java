package com.logistic.ontologies.dto.rule;

import java.util.List;

public record BuiltInAtom(
        String builtInFunction,
        List<Argument> arguments
) implements Atom {
}