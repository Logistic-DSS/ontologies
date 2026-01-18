package com.logistic.ontologies.service.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.logistic.ontologies.service.ontology.OntologyStorage;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BaseDomainService {

    @Autowired
    protected OntologyStorage ontologyStorage;

    private OWLLiteral toLiteral(OWLDataFactory df, Object value) {
        if (value instanceof String s)
            return df.getOWLLiteral(s);

        if (value instanceof Integer i)
            return df.getOWLLiteral(i);

        if (value instanceof Double d)
            return df.getOWLLiteral(d);

        if (value instanceof Boolean b)
            return df.getOWLLiteral(b);

        if (value instanceof Instant t)
            return df.getOWLLiteral(t.toString(), OWL2Datatype.XSD_DATE_TIME);

        if (value instanceof UUID u)
            return df.getOWLLiteral(u.toString());

        throw new IllegalArgumentException("Unsupported literal type");
    }

    private <T> T convertLiteral(OWLLiteral literal, Class<T> type) {
        if (type == String.class)
            return (T) literal.getLiteral();

        if (type == Integer.class)
            return (T) Integer.valueOf(literal.parseInteger());

        if (type == Double.class)
            return (T) Double.valueOf(literal.parseDouble());

        if (type == Boolean.class)
            return (T) Boolean.valueOf(literal.parseBoolean());

        if (type == Instant.class)
            return (T) Instant.parse(literal.getLiteral());

        if (type == UUID.class)
            return (T) UUID.fromString(literal.getLiteral());

        throw new IllegalArgumentException(
            "Unsupported datatype: " + type.getName()
        );
    }

    protected OWLOntology loadTaskOntology(UUID taskId) throws OWLOntologyCreationException {
        try {
            return ontologyStorage.loadTaskOntology(taskId);
        } catch (OWLOntologyCreationException e) {
            throw new OWLOntologyCreationException("Eror when trying to load ontology");
        }
    }

    protected void save(OWLOntology ontology) throws OWLOntologyStorageException {
        try {
            ontologyStorage.saveOntology(ontology);
        } catch (OWLOntologyStorageException e) {
            throw new OWLOntologyStorageException("Error when trying to save ontology");
        }
    }

    protected UUID getIDFromURI(IRI iri) {
        return UUID.fromString(
            iri.getShortForm().substring(iri.getShortForm().indexOf(":")+1)
        );
    }

    protected String getStrIDFromURI(IRI iri) {
        return
            iri.getShortForm().substring(iri.getShortForm().indexOf(":")+1);
    }

    protected IRI getIRIFromIDAndClass(OWLOntology ontology, String id, String className) {
        return IRI.create(ontology.getOntologyID().getOntologyIRI().orElseThrow() + "#" + className + ":" + id);
    }

    protected OWLNamedIndividual createIndividual(OWLOntology ontology, String className, UUID id) {
        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        IRI iri = getIRIFromIDAndClass(ontology, id.toString(), className);
        OWLNamedIndividual individual = factory.getOWLNamedIndividual(iri);
        OWLClass owlClass = ontologyStorage.getClassByName(className);
        OWLClassAssertionAxiom axiom = factory.getOWLClassAssertionAxiom(owlClass, individual);
        ontology.addAxiom(axiom);
        return individual;
    }

    protected OWLNamedIndividual requireIndividual(OWLOntology ontology, String className, UUID id) {
        IRI iri = getIRIFromIDAndClass(ontology, id.toString(), className);
        OWLNamedIndividual individual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(iri);
        OWLClass owlClass = ontologyStorage.getClassByName(className);
        boolean hasCorrectClass = ontology.getClassAssertionAxioms(individual)
            .stream()
            .anyMatch(ax -> ax.getClassExpression().equals(owlClass));
        if (hasCorrectClass) throw new EntityNotFoundException("Individual not found or has wrong class: " + id);
        return individual;
    }

    protected OWLNamedIndividual requireIndividual(OWLOntology ontology, String className, String id) {
        IRI iri = getIRIFromIDAndClass(ontology, id, className);
        OWLNamedIndividual individual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(iri);
        OWLClass owlClass = ontologyStorage.getClassByName(className);
        boolean hasCorrectClass = ontology.getClassAssertionAxioms(individual)
            .stream()
            .anyMatch(ax -> ax.getClassExpression().equals(owlClass));
        if (hasCorrectClass) throw new EntityNotFoundException("Individual not found or has wrong class: " + id);
        return individual;
    }

    protected List<OWLNamedIndividual> requireAllIndividuals(OWLOntology ontology, String className) {
        OWLClass owlClass = ontologyStorage.getClassByName(className);
        return ontology.getClassAssertionAxioms(owlClass).stream()
            .map(ax -> ax.getIndividual().asOWLNamedIndividual())
            .toList();
    }

    protected void setDataProperty(OWLOntology ontology, OWLNamedIndividual individual, String propertyName, Object value) {
        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLDataProperty property = ontologyStorage.getDataPropertyByName(propertyName);

        ontology.getDataPropertyAssertionAxioms(individual)
                .stream()
                .filter(a -> a.getProperty().equals(property))
                .forEach(ontology::removeAxiom);

        OWLLiteral literal = toLiteral(factory, value);
        OWLDataPropertyAssertionAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(property, individual, literal);

        ontology.addAxiom(axiom);
    }

    protected <T> T getDataPropertyValue(OWLOntology ontology, OWLNamedIndividual individual, String propertyName, Class<T> targetType) {
        OWLDataProperty property = ontologyStorage.getDataPropertyByName(propertyName);
        return ontology.getDataPropertyAssertionAxioms(individual).stream()
            .filter(ax -> ax.getProperty().asOWLDataProperty().equals(property))
            .map(OWLDataPropertyAssertionAxiom::getObject)
            .findFirst()
            .map(literal -> convertLiteral(literal, targetType))
            .orElse(null);
    }

    protected void addObjectProperty(OWLOntology ontology, OWLNamedIndividual subject, String propertyName, OWLNamedIndividual object) {
        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLObjectProperty property = ontologyStorage.getObjectPropertyByName(propertyName);
        OWLObjectPropertyAssertionAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
        ontology.addAxiom(axiom);
    }

    protected List<OWLNamedIndividual> getObjectPropertyValues(OWLOntology ontology, OWLNamedIndividual subject, String propertyName) {
        OWLObjectProperty property = ontologyStorage.getObjectPropertyByName(propertyName);

        return ontology.getObjectPropertyAssertionAxioms(subject)
            .stream()
            .filter(ax -> ax.getProperty().asOWLObjectProperty().equals(property))
            .map(OWLObjectPropertyAssertionAxiom::getObject)
            .filter(OWLIndividual::isNamed)
            .map(OWLIndividual::asOWLNamedIndividual)
            .toList();
    }

    protected void deleteObjectProperty(OWLOntology ontology, OWLNamedIndividual subject, String propertyName, OWLNamedIndividual object) {
        OWLObjectProperty property = ontologyStorage.getObjectPropertyByName(propertyName);
        ontology.getObjectPropertyAssertionAxioms(subject).stream()
            .filter(ax -> ax.getProperty().equals(property) && ax.getObject().equals(object))
            .forEach(ontology::removeAxiom);
    }

    protected void deleteIndividual(OWLOntology ontology, OWLNamedIndividual individual) {
        ontology.getAxioms(individual).forEach(ontology::removeAxiom);
    }
}