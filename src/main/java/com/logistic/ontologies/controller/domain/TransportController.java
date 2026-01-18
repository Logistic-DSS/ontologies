package com.logistic.ontologies.controller.domain;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.logistic.ontologies.dto.ErrorResponse;
import com.logistic.ontologies.dto.domain.TransportCreateDTO;
import com.logistic.ontologies.service.domain.TransportService;
import com.logistic.ontologies.util.SecurityUtil;

import jakarta.persistence.EntityNotFoundException;

@Service
@RequestMapping("/api/tasks/{taskId}/trasports")
public class TransportController {

    @Autowired
    private TransportService service;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("")
    public ResponseEntity<?> createTransport(@PathVariable UUID taskId, @RequestBody TransportCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.createTransport(taskId, userId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Server can not create transport"));
        }
    }

    @GetMapping("/{transportId}")
    public ResponseEntity<?> getTransport(@PathVariable UUID taskId, @PathVariable UUID transportId) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.getTransport(taskId, userId, transportId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getTransports(@PathVariable UUID taskId) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.getAllTransports(taskId, userId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{transportId}")
    public ResponseEntity<?> updateTransport(@PathVariable UUID taskId, @PathVariable UUID transportId, @RequestBody TransportCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            service.updateTransport(taskId, userId, transportId, dto);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{transportId}")
    public ResponseEntity<?> deleteTransport(@PathVariable UUID taskId, @PathVariable UUID transportId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.deleteTransport(taskId, userId, transportId);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{transportId}/cargos/{cargoId}")
    public ResponseEntity<?> assignCargo(@PathVariable UUID taskId, @PathVariable UUID transportId, @PathVariable UUID cargoId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.assignCargo(taskId, userId, transportId, cargoId);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{transportId}/cargos/{cargoId}")
    public ResponseEntity<?> unassignCargo(@PathVariable UUID taskId, @PathVariable UUID transportId, @PathVariable UUID cargoId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.unassignCargo(taskId, userId, transportId, cargoId);
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
