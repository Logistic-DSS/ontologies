package com.logistic.ontologies.service.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SWRLRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistic.ontologies.dto.rule.RuleCreateDTO;
import com.logistic.ontologies.dto.rule.RuleDTO;
import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserRule;
import com.logistic.ontologies.repository.RuleOntologyRepository;
import com.logistic.ontologies.repository.UserRuleRoleRepository;
import com.logistic.ontologies.service.ontology.OntologyStorage;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RuleService {

    @Autowired
    private RuleOntologyRepository ruleRepo;
    @Autowired
    private UserRuleRoleRepository roleRepo;
    @Autowired
    private OntologyStorage ontologyStorage;
    @Autowired
    private RuleParser ruleParser;
    @Value("${ontology.base.ruleIDIRI}")
    private String ruleIDIRI;

    @Transactional
    public RuleDTO createRule(UUID userId, UUID ruleOntologyId, RuleCreateDTO dto) throws OWLOntologyCreationException, OWLOntologyStorageException {
        ruleRepo.findById(ruleOntologyId)
            .orElseThrow(() -> new EntityNotFoundException("Rules ontology not found"));
        
        UserRule role = roleRepo.findByIdUserIdAndIdRuleId(userId, ruleOntologyId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't create rules");

        OWLOntology ruleOntology =  ontologyStorage.loadRuleOntology(ruleOntologyId);

        OWLDataFactory factory = ruleOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLAnnotationProperty idProperty = factory.getOWLAnnotationProperty(IRI.create(ruleIDIRI));

        SWRLRule rule = ruleParser.toSwrlRule(dto, ruleOntology);

        System.out.println("Новое правило: " + rule.toString());

        UUID ruleID = UUID.randomUUID();

        OWLAnnotation annotation = factory.getOWLAnnotation(idProperty, factory.getOWLLiteral(ruleID.toString()));
        OWLAxiom annotatedAxiom = rule.getAnnotatedAxiom(Collections.singleton(annotation));

        ruleOntology.addAxiom(annotatedAxiom);

        ontologyStorage.saveOntology(ruleOntology);

        return new RuleDTO(
            ruleOntologyId,
            ruleID,
            dto.antecedent(),
            dto.consequent()
        );
    }

    @Transactional
    public void updateRule(UUID userId, UUID ruleOntologyId, UUID ruleId, RuleCreateDTO dto) throws OWLException {
        ruleRepo.findById(ruleOntologyId)
            .orElseThrow(() -> new EntityNotFoundException("Rules ontology not found"));
        
        UserRule role = roleRepo.findByIdUserIdAndIdRuleId(userId, ruleOntologyId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't edit rules");

        this.deleteRule(userId, ruleOntologyId, ruleId);

        OWLOntology ruleOntology =  ontologyStorage.loadRuleOntology(ruleOntologyId);

        OWLDataFactory factory = ruleOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLAnnotationProperty idProperty = factory.getOWLAnnotationProperty(IRI.create(ruleIDIRI));

        SWRLRule rule = ruleParser.toSwrlRule(dto, ruleOntology);

        OWLAnnotation annotation = factory.getOWLAnnotation(idProperty, factory.getOWLLiteral(ruleId.toString()));
        OWLAxiom annotatedAxiom = rule.getAnnotatedAxiom(Collections.singleton(annotation));

        ruleOntology.addAxiom(annotatedAxiom);

        ontologyStorage.saveOntology(ruleOntology);
    }

    @Transactional
    public void deleteRule(UUID userId, UUID ruleOntologyId, UUID ruleId) throws OWLException {
        ruleRepo.findById(ruleOntologyId)
            .orElseThrow(() -> new EntityNotFoundException("Rules ontology not found"));
        
        UserRule role = roleRepo.findByIdUserIdAndIdRuleId(userId, ruleOntologyId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't delete rules");

        OWLOntology ruleOntology =  ontologyStorage.loadRuleOntology(ruleOntologyId);

        for (SWRLRule swrlRule: ruleOntology.getAxioms(AxiomType.SWRL_RULE)) {
            for (OWLAnnotation annotation : swrlRule.getAnnotations()) {
                OWLAnnotationValue value = annotation.getValue();
                String curRuleID = value.asLiteral().get().toString();
                curRuleID = curRuleID.substring(1, curRuleID.indexOf("\"^^xsd:string"));
                if (curRuleID.equals(ruleId.toString())) {
                    ruleOntology.removeAxiom(swrlRule.getAnnotatedAxiom(Collections.singleton(annotation)));
                    ruleOntology.removeAxiom(swrlRule);
                    ontologyStorage.saveOntology(ruleOntology);
                    return;
                }
            }
        }
        throw new EntityNotFoundException("Rule doesn't exist");
    }

    @Transactional(readOnly = true)
    public RuleDTO getRule(UUID userId, UUID ruleOntologyId, UUID ruleId) throws OWLException {
        ruleRepo.findById(ruleOntologyId)
            .orElseThrow(() -> new EntityNotFoundException("Rules ontology not found"));
        
        roleRepo.findByIdUserIdAndIdRuleId(userId, ruleOntologyId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology ruleOntology =  ontologyStorage.loadRuleOntology(ruleOntologyId);

        for (SWRLRule swrlRule: ruleOntology.getAxioms(AxiomType.SWRL_RULE)) {
            for (OWLAnnotation annotation : swrlRule.getAnnotations()) {
                    OWLAnnotationValue value = annotation.getValue();
                    String curRuleID = value.asLiteral().get().toString();
                    curRuleID = curRuleID.substring(1, curRuleID.indexOf("\"^^xsd:string"));
                    if (curRuleID.equals(ruleId.toString())) {
                        return ruleParser.fromSwrlRule(swrlRule, ruleOntology, ruleOntologyId, ruleId);
                    }
                }
            }
        throw new EntityNotFoundException("Rule doesn't exist");
    }

    @Transactional(readOnly = true)
    public ArrayList<RuleDTO> getRules(UUID userId, UUID ruleOntologyId) throws OWLException {
        ruleRepo.findById(ruleOntologyId)
            .orElseThrow(() -> new EntityNotFoundException("Rules ontology not found"));
        
        roleRepo.findByIdUserIdAndIdRuleId(userId, ruleOntologyId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology ruleOntology =  ontologyStorage.loadRuleOntology(ruleOntologyId);

        ArrayList<RuleDTO> rules = new ArrayList<RuleDTO>();
        for (SWRLRule swrlRule: ruleOntology.getAxioms(AxiomType.SWRL_RULE)) {
            UUID ruleID = null;
            for (OWLAnnotation annotation : swrlRule.getAnnotations()) {
                OWLAnnotationValue value = annotation.getValue();
                String curRuleID = value.asLiteral().get().toString();
                ruleID = UUID.fromString(curRuleID.substring(1, curRuleID.indexOf("\"^^xsd:string")));
                break;
            }
            rules.add(ruleParser.fromSwrlRule(swrlRule, ruleOntology, ruleOntologyId, ruleID));
        }
        return rules;
    }

}
