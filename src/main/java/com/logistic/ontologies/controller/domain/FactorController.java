package com.logistic.ontologies.controller.domain;

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
import com.logistic.ontologies.dto.domain.FactorCreateDTO;
import com.logistic.ontologies.service.domain.FactorService;
import com.logistic.ontologies.util.SecurityUtil;

import jakarta.persistence.EntityNotFoundException;

@Controller
@RequestMapping("/api/tasks/{taskId}/factors")
public class FactorController {

    @Autowired
    private FactorService service;

    @Autowired
    private SecurityUtil securityUtil;
    
    @PostMapping("")
    public ResponseEntity<?> createFactor(@PathVariable UUID taskId, @RequestBody FactorCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.createFactor(taskId, userId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Server can not create factor"));
        }
    }

    @GetMapping("/{factorId}")
    public ResponseEntity<?> getFactor(@PathVariable UUID taskId, @PathVariable UUID factorId) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.getFactor(taskId, userId, factorId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getFactors(@PathVariable UUID taskId) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.getAllFactors(taskId, userId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{factorId}")
    public ResponseEntity<?> updateFactor(@PathVariable UUID taskId, @PathVariable UUID factorId, @RequestBody FactorCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            service.updateFactor(taskId, userId, factorId, dto);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{factorId}")
    public ResponseEntity<?> deleteFactor(@PathVariable UUID taskId, @PathVariable UUID factorId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.deleteFactor(taskId, userId, factorId);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }
}