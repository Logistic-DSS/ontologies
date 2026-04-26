package com.logistic.ontologies.service.reasoning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import com.logistic.ontologies.dto.reasoning.InferenceStep;
import com.logistic.ontologies.dto.reasoning.ReasoningResult;
import com.logistic.ontologies.model.TaskRuleOrder;
import com.logistic.ontologies.repository.TaskOntologyRepository;
import com.logistic.ontologies.repository.TaskRuleOrderRepository;
import com.logistic.ontologies.repository.UserTaskRoleRepository;
import com.logistic.ontologies.service.ontology.OntologyStorage;

import jakarta.persistence.EntityNotFoundException;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;

@Service
public class ReasoningService {

    @Autowired
    private TaskRuleOrderRepository taskRuleRepo;
    @Autowired
    private TaskOntologyRepository taskRepo;
    @Autowired
    private UserTaskRoleRepository roleRepo;
    @Autowired
    private OntologyStorage ontologyStorage;

    @Transactional(readOnly = true)
    public ReasoningResult run(UUID userId, UUID taskOntologyId) throws OWLOntologyCreationException {
        taskRepo.findById(taskOntologyId)
            .orElseThrow(() -> new EntityNotFoundException("Task ontology not found"));
        
        roleRepo.findByIdUserIdAndIdTaskId(userId, taskOntologyId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology taskOntology;
        taskOntology = ontologyStorage.loadTaskOntology(taskOntologyId);

        List<TaskRuleOrder> ruleOntologies = taskRuleRepo.findByIdTaskIdOrderByOrderNumberAsc(taskOntologyId);

        if (ruleOntologies == null || ruleOntologies.isEmpty())
            throw new IllegalStateException("No rule ontologies assigned");

        OWLOntology workingOntology = createWorkingOntology(taskOntology);

        List<InferenceStep> steps = new ArrayList<>();

        for (TaskRuleOrder ruleOntologyOrder : ruleOntologies) {
            UUID ruleId = ruleOntologyOrder.getId().getRuleId();
            OWLOntology ruleOntology = ontologyStorage.loadRuleOntology(ruleId);

            InferenceStep step = applyRules(
                workingOntology,
                ruleOntology,
                ruleId
            );

            steps.add(step);
        }
        
        return new ReasoningResult(taskOntologyId, steps);
    }

    private OWLOntology createWorkingOntology(OWLOntology source) throws OWLOntologyCreationException {
        OWLOntologyManager manager = source.getOWLOntologyManager();
        OWLOntology temp = manager.createOntology();
        manager.addAxioms(temp, source.getAxioms());
        return temp;
    }

    private InferenceStep applyRules(OWLOntology workingOntology, OWLOntology ruleOntology, UUID ruleOntologyId) {
        OWLOntologyManager manager = workingOntology.getOWLOntologyManager();

        Set<SWRLRule> rules = ruleOntology.getAxioms(AxiomType.SWRL_RULE);
        manager.addAxioms(workingOntology, rules);

        OpenlletReasonerFactory rf = OpenlletReasonerFactory.getInstance();
        OpenlletReasoner reasoner = rf.createReasoner(workingOntology);

        reasoner.precomputeInferences();

        Set<OWLAxiom> inferred = extractInferredAxioms(reasoner, workingOntology);
        manager.addAxioms(workingOntology, inferred);

        workingOntology.removeAxioms(rules);

        return new InferenceStep(
            ruleOntologyId,
            inferred.stream().map(axiom -> axiom.toString()).toList()
        );
    }

    private Set<OWLAxiom> extractInferredAxioms(OpenlletReasoner reasoner, OWLOntology ontology) {

        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();

        Set<OWLAxiom> asserted = ontology.getABoxAxioms(Imports.INCLUDED);

        Set<OWLAxiom> inferred = new HashSet<>();

        for (OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {

            NodeSet<OWLClass> types = reasoner.getTypes(individual, false);

            for (OWLClass cls : types.getFlattened()) {
                inferred.add(factory.getOWLClassAssertionAxiom(cls, individual));
            }

            for (OWLObjectProperty prop : ontology.objectPropertiesInSignature().toList()) {

                NodeSet<OWLNamedIndividual> values =
                    reasoner.getObjectPropertyValues(individual, prop);

                for (OWLNamedIndividual value : values.getFlattened()) {
                    inferred.add(factory.getOWLObjectPropertyAssertionAxiom(
                        prop, individual, value
                    ));
                }
            }

            for (OWLDataProperty prop : ontology.dataPropertiesInSignature().toList()) {

                Set<OWLLiteral> values =
                    reasoner.getDataPropertyValues(individual, prop);

                for (OWLLiteral val : values) {
                    inferred.add(factory.getOWLDataPropertyAssertionAxiom(
                        prop, individual, val
                    ));
                }
            }
        }
        inferred.removeAll(asserted);
        inferred.removeIf(ax ->
            ax.toString().contains("owl:Thing") ||
            ax.toString().contains("topDataProperty") ||
            ax.toString().contains("topObjectProperty")
        );
        return inferred;
    }
}