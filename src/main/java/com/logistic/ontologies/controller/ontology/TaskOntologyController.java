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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.logistic.ontologies.dto.ErrorResponse;
import com.logistic.ontologies.dto.ontology.OntologyCreateDTO;
import com.logistic.ontologies.service.ontology.TaskOntologyService;
import com.logistic.ontologies.util.SecurityUtil;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.PutMapping;

@RequestMapping(path = "/api/tasks")
@Controller
public class TaskOntologyController {

    @Autowired
    private TaskOntologyService service;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("")
    public ResponseEntity<?> createTask(@RequestBody OntologyCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.createTaskOntology(userId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                // new ErrorResponse("Server can not create ontology")
                e.getMessage()
            );
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTask(@PathVariable UUID taskId) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.getTaskOntology(taskId, userId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getTasks() {
        UUID userId = securityUtil.getUserId();
        return ResponseEntity.ok(service.getAllTaskOntologies(userId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable UUID taskId, @RequestBody OntologyCreateDTO dto) {
        UUID userId = securityUtil.getUserId();
        try {
            service.updateTaskOntology(taskId, userId, dto);
            return (ResponseEntity<?>) ResponseEntity.ok();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable UUID taskId) {
        UUID userId = securityUtil.getUserId();
        try {
            service.deleteTaskOntology(taskId, userId);
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
