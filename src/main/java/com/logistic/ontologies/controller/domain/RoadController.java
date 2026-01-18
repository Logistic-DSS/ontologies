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
import com.logistic.ontologies.dto.domain.RoadCreateDTO;
import com.logistic.ontologies.service.domain.RoadService;
import com.logistic.ontologies.util.SecurityUtil;

import jakarta.persistence.EntityNotFoundException;

@Service
@RequestMapping("/api/tasks/{taskId}/roads")
public class RoadController {

    @Autowired
    private RoadService service;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("")
    public ResponseEntity<?> createTransport(@PathVariable UUID taskId, @RequestBody RoadCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.createRoad(taskId, userId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Server can not create road"));
        }
    }

    @GetMapping("/{roadId}")
    public ResponseEntity<?> getTransport(@PathVariable UUID taskId, @PathVariable UUID roadId) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.getRoad(taskId, userId, roadId));
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
            return ResponseEntity.ok(service.getAllRoads(taskId, userId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{roadId}")
    public ResponseEntity<?> updateTransport(@PathVariable UUID taskId, @PathVariable UUID roadId, @RequestBody RoadCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            service.updateRoad(taskId, userId, roadId, dto);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{roadId}")
    public ResponseEntity<?> deleteTransport(@PathVariable UUID taskId, @PathVariable UUID roadId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.deleteRoad(taskId, userId, roadId);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{roadId}/factors/{factorId}")
    public ResponseEntity<?> assignCargo(@PathVariable UUID taskId, @PathVariable UUID roadId, @PathVariable UUID factorId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.assignFactor(taskId, userId, roadId, factorId);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{roadId}/factors/{factorId}")
    public ResponseEntity<?> unassignCargo(@PathVariable UUID taskId, @PathVariable UUID roadId, @PathVariable UUID factorId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.unassignFactor(taskId, userId, roadId, factorId);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{roadId}/weather/{weatherId}")
    public ResponseEntity<?> assignWeather(@PathVariable UUID taskId, @PathVariable UUID roadId, @PathVariable String weatherId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.assignWeather(taskId, userId, roadId, weatherId);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{roadId}/weather/{weatherId}")
    public ResponseEntity<?> unassignWeather(@PathVariable UUID taskId, @PathVariable UUID roadId, @PathVariable String weatherId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.unassignWeather(taskId, userId, roadId, weatherId);
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
