package com.logistic.ontologies.controller.domain;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.logistic.ontologies.dto.ErrorResponse;
import com.logistic.ontologies.dto.domain.CargoCreateDTO;
import com.logistic.ontologies.service.domain.CargoService;
import com.logistic.ontologies.util.SecurityUtil;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/api/tasks/{taskId}/cargos")
public class CargoController {

    @Autowired
    private CargoService service;

    @Autowired
    private SecurityUtil securityUtil;
    
    @PostMapping("")
    public ResponseEntity<?> createCargo(@PathVariable UUID taskId, @RequestBody CargoCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.createCargo(taskId, userId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Server can not create cargo"));
        }
    }

    @GetMapping("/{cargoId}")
    public ResponseEntity<?> getCargo(@PathVariable UUID taskId, @PathVariable UUID cargoId) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.getCargo(taskId, userId, cargoId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getCargos(@PathVariable UUID taskId) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.getAllCargos(taskId, userId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{cargoId}")
    public ResponseEntity<?> updateCargo(@PathVariable UUID taskId, @PathVariable UUID cargoId, @RequestBody CargoCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            service.updateCargo(taskId, userId, cargoId, dto);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{cargoId}")
    public ResponseEntity<?> deleteCargo(@PathVariable UUID taskId, @PathVariable UUID cargoId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.deleteCargo(taskId, userId, cargoId);
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