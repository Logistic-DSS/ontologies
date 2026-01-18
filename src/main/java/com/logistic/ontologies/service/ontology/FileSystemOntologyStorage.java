package com.logistic.ontologies.service.ontology;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class FileSystemOntologyStorage implements OntologyStorage {
    @Value("${ontology.base.path}")
    private String ontologiesDirectory;
    @Value("${ontology.base.IRI}")
    private String baseOntologyIRI;
    @Value("${ontology.base.taskIRI}")
    private String taskIDIRI;
    @Value("${ontology.base.ruleIRI}")
    private String ruleIDIRI;

    private final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static OWLOntology base;

    private Path ROOT;

    @PostConstruct
    void init() throws IOException, OWLOntologyCreationException {
        this.ROOT = Path.of(ontologiesDirectory);
        Files.createDirectories(ROOT.resolve("tasks"));
        Files.createDirectories(ROOT.resolve("rules"));

        IRI baseIri = IRI.create(baseOntologyIRI);
        IRI docIri = IRI.create(
            new ClassPathResource("ontologies/base.owx").getURL()
        );

        manager.getIRIMappers().add(
            new SimpleIRIMapper(baseIri, docIri)
        );

        manager.getIRIMappers().add(iri -> {
            String iriStr = iri.toString();

            if (iriStr.startsWith(taskIDIRI)) {
                String id = iriStr.substring(taskIDIRI.length());
                return IRI.create(ROOT.resolve("tasks").resolve(id + ".owl").toUri());
            }

            if (iriStr.startsWith(ruleIDIRI)) {
                String id = iriStr.substring(ruleIDIRI.length());
                return IRI.create(ROOT.resolve("rules").resolve(id + ".owl").toUri());
            }

            return null;
        });

        base = manager.loadOntologyFromOntologyDocument(docIri);
    }

    @Override
    public void createTaskOntology(UUID taskId) throws OWLOntologyCreationException {
        try {
            IRI iri = IRI.create(taskIDIRI + taskId);
            OWLOntology ontology = manager.createOntology(iri);

            OWLImportsDeclaration imports = manager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create(baseOntologyIRI));
            manager.applyChange(new AddImport(ontology, imports));

            saveOntology(ontology);
        } catch (OWLOntologyStorageException e) {
            throw new OWLOntologyCreationException();
        }
    }

    @Override
    public void createRuleOntology(UUID ruleId) throws OWLOntologyCreationException {
        try {
            IRI iri = IRI.create(ruleIDIRI + ruleId);
            OWLOntology ontology = manager.createOntology(iri);

            OWLImportsDeclaration imports = manager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create(baseOntologyIRI));
            manager.applyChange(new AddImport(ontology, imports));

            saveOntology(ontology);
        } catch (OWLOntologyStorageException e) {
            throw new OWLOntologyCreationException();
        }
    }

    @Override
    public OWLOntology loadTaskOntology(UUID taskId) throws OWLOntologyCreationException {
        return manager.loadOntology(IRI.create(taskIDIRI + taskId));
    }

    @Override
    public OWLOntology loadRuleOntology(UUID ruleId) throws OWLOntologyCreationException {
        return manager.loadOntology(IRI.create(ruleIDIRI + ruleId));
    }

    @Override
    public void saveOntology(OWLOntology ontology) throws OWLOntologyStorageException {
        Path path = resolvePath(ontology.getOntologyID().getOntologyIRI().orElseThrow());
        manager.saveOntology(ontology, IRI.create(path.toUri()));
    }

    private Path resolvePath(IRI iri) {
        String iriString = iri.toString();

        if (iriString.startsWith(taskIDIRI)) {
            String id = iriString.substring(taskIDIRI.length());
            return ROOT.resolve("tasks").resolve(id + ".owl");
        }

        if (iriString.startsWith(ruleIDIRI)) {
            String id = iriString.substring(ruleIDIRI.length());
            return ROOT.resolve("rules").resolve(id + ".owl");
        }

        throw new IllegalArgumentException("Unknown ontology IRI: " + iri);
    }

    @Override
    public void deleteTaskOntology(UUID taskId) throws IOException {
        Files.deleteIfExists(ROOT.resolve("tasks/" + taskId + ".owl"));
    }

    @Override
    public void deleteRuleOntology(UUID ruleId) throws IOException {
        Files.deleteIfExists(ROOT.resolve("rules/" + ruleId + ".owl"));
    }

    @Override
    public OWLClass getClassByName(String className) {
        return manager.getOWLDataFactory().getOWLClass(IRI.create(baseOntologyIRI + "#" + className));
    }

    @Override
    public OWLDataProperty getDataPropertyByName(String dataPropertyName) {
        return manager.getOWLDataFactory().getOWLDataProperty(IRI.create(baseOntologyIRI + "#" + dataPropertyName));
    }

    @Override
    public OWLObjectProperty getObjectPropertyByName(String objectPropertyName) {
        return manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(baseOntologyIRI + "#" + objectPropertyName));
    }
}
