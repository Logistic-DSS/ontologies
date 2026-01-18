package com.logistic.ontologies.service.ontology;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistic.ontologies.dto.ontology.OntologyCreateDTO;
import com.logistic.ontologies.dto.ontology.OntologyDTO;
import com.logistic.ontologies.model.RuleOntology;
import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserRule;
import com.logistic.ontologies.repository.RuleOntologyRepository;
import com.logistic.ontologies.repository.UserRuleRoleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RuleOntologyService {
    @Autowired
    private RuleOntologyRepository ruleRepo;
    @Autowired
    private UserRuleRoleRepository roleRepo;
    @Autowired
    private OntologyStorage ontologyStorage;

    @Transactional
    public OntologyDTO createRuleOntology(UUID ownerId, OntologyCreateDTO dto) throws OWLOntologyCreationException {
        RuleOntology rule = new RuleOntology();
        rule.setId(UUID.randomUUID());
        rule.setOwnerId(ownerId);
        rule.setName(dto.name());
        rule.setDescription(dto.description());
        ruleRepo.save(rule);

        UserRule role = new UserRule(rule.getOwnerId(), rule.getId());

        roleRepo.save(role);

        ontologyStorage.createRuleOntology(rule.getId());

        return new OntologyDTO(
            rule.getId(),
            rule.getOwnerId(),
            rule.getName(),
            rule.getDescription(),
            Date.from(rule.getCreatedAt()),
            Date.from(rule.getUpdatedAt())
        );
    }

    @Transactional(readOnly = true)
    public OntologyDTO getRuleOntology(UUID ruleId, UUID userId) {
        RuleOntology rule =  ruleRepo.findById(ruleId)
            .orElseThrow(() -> new EntityNotFoundException("Rules ontology not found"));
        
        roleRepo.findByIdUserIdAndIdRuleId(userId, ruleId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        return new OntologyDTO(
            rule.getId(),
            rule.getOwnerId(),
            rule.getName(),
            rule.getDescription(),
            Date.from(rule.getCreatedAt()),
            Date.from(rule.getUpdatedAt())
        );
    }

    @Transactional(readOnly = true)
    public List<OntologyDTO> getAllRuleOntologies(UUID userId) {
        List<UserRule> rules = roleRepo.findAllByIdUserId(userId);

        ArrayList<OntologyDTO> res = new ArrayList<OntologyDTO>();
        for (UserRule rule : rules) {
            UUID ruleId = rule.getId().getRuleId();
            res.add(getRuleOntology(ruleId, userId));
        }

        return res;
    }

    @Transactional
    public void deleteRuleOntology(UUID ruleId, UUID userId) throws IOException {
        ruleRepo.findById(ruleId)
            .orElseThrow(() -> new EntityNotFoundException("Rules ontology not found"));
        
        UserRule role = roleRepo.findByIdUserIdAndIdRuleId(userId, ruleId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() != UserRole.OWNER)
            throw new AccessDeniedException("Only owner can delete rules ontology");

        ontologyStorage.deleteRuleOntology(ruleId);
        ruleRepo.deleteById(ruleId);
    }

    @Transactional
    public void updateRuleOntology(UUID ruleId, UUID userId, OntologyCreateDTO dto) {

        RuleOntology rule =  ruleRepo.findById(ruleId)
            .orElseThrow(() -> new EntityNotFoundException("Rules ontology not found"));
        
        UserRule role = roleRepo.findByIdUserIdAndIdRuleId(userId, ruleId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't edit rules ontology properties");

        rule.setName(dto.name());
        rule.setDescription(dto.description());
        rule.setUpdatedAt(Instant.now());
        ruleRepo.save(rule);
    }
}