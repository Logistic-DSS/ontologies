package com.logistic.ontologies.controller.ontology;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.logistic.ontologies.dto.ErrorResponse;
import com.logistic.ontologies.dto.ontology.OntologyCreateDTO;
import com.logistic.ontologies.service.ontology.RuleOntologyService;
import com.logistic.ontologies.util.SecurityUtil;

import jakarta.persistence.EntityNotFoundException;

@Controller
@RequestMapping(path = "/api/rules")
public class RuleOntologyController {

    @Autowired
    private RuleOntologyService service;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("")
    public ResponseEntity<?> createRule(@RequestBody OntologyCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.createRuleOntology(userId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Server can not create ontology"));
        }
    }

    @GetMapping("/{ruleId}")
    public ResponseEntity<?> getRule(@PathVariable UUID ruleId) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.getRuleOntology(ruleId, userId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getRules() {
        UUID userId = securityUtil.getUserId();
        return ResponseEntity.ok(service.getAllRuleOntologies(userId));
    }

    @PutMapping("/{ruleId}")
    public ResponseEntity<?> updateRule(@PathVariable UUID ruleId, @RequestBody OntologyCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            service.updateRuleOntology(ruleId, userId, dto);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<?> deleteRule(@PathVariable UUID ruleId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.deleteRuleOntology(ruleId, userId);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch(IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Task not found"));
        }
    }
}