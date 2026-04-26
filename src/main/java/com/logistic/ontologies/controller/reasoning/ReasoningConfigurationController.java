package com.logistic.ontologies.controller.reasoning;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.logistic.ontologies.dto.ErrorResponse;
import com.logistic.ontologies.dto.reasoning.OrderDTO;
import com.logistic.ontologies.service.reasoning.ReasoningConfigurationService;
import com.logistic.ontologies.util.SecurityUtil;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping(path = "/api/reasoning/configuration")
@Controller
public class ReasoningConfigurationController {

    @Autowired
    private ReasoningConfigurationService service;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("")
    public ResponseEntity<?> bindRule(@RequestBody OrderDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            service.bind(userId, dto);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch(IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{taskId}/{ruleId}")
    // public ResponseEntity<?> changeRuleOrder(@RequestBody OrderDTO dto) {
    public ResponseEntity<?> changeRuleOrder(@PathVariable UUID taskId, @PathVariable UUID ruleId, @RequestParam Integer order) {
        UUID userId = securityUtil.getUserId();
        try {
            // service.changeOrder(userId, dto);
            service.changeOrder(userId, taskId, ruleId, order);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{taskId}/{ruleId}")
    public ResponseEntity<?> unbindRule(@PathVariable UUID taskId, @PathVariable UUID ruleId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.unbind(userId, taskId, ruleId);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getBindedRules(@PathVariable UUID taskId) {
        UUID userId = securityUtil.getUserId();
        try {
            return (ResponseEntity<?>) ResponseEntity.ok(service.getOrderedRules(taskId, userId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }
}