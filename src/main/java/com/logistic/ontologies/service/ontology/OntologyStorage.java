package com.logistic.ontologies.service.ontology;

import java.io.IOException;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public interface OntologyStorage {
    
    void createTaskOntology(UUID taskId) throws OWLOntologyCreationException;
    void createRuleOntology(UUID ruleId) throws OWLOntologyCreationException;

    OWLOntology loadTaskOntology(UUID taskId) throws OWLOntologyCreationException;
    OWLOntology loadRuleOntology(UUID ruleId) throws OWLOntologyCreationException;

    void saveOntology(OWLOntology ontology) throws OWLOntologyStorageException;

    void deleteTaskOntology(UUID taskId) throws IOException;
    void deleteRuleOntology(UUID ruleId) throws IOException;

    OWLClass getClassByName(String className);
    OWLDataProperty getDataPropertyByName(String dataPropertyName);
    OWLObjectProperty getObjectPropertyByName(String objectPropertyName);
}