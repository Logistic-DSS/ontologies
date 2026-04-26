package com.logistic.ontologies.dto.rule;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ClassAtom.class, name = "ClassAtom"),
    @JsonSubTypes.Type(value = ObjectPropertyAtom.class, name = "ObjectPropertyAtom"),
    @JsonSubTypes.Type(value = DataPropertyAtom.class, name = "DataPropertyAtom"),
    @JsonSubTypes.Type(value = BuiltInAtom.class, name = "BuiltInAtom")
})
public interface Atom {
}