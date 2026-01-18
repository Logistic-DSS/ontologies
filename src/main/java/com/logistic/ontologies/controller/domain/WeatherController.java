package com.logistic.ontologies.controller.domain;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.logistic.ontologies.dto.ErrorResponse;
import com.logistic.ontologies.service.domain.WeatherService;
import com.logistic.ontologies.util.SecurityUtil;

import jakarta.persistence.EntityNotFoundException;

@Controller
@RequestMapping("/api/tasks/{taskId}/weathers")
public class WeatherController {

    @Autowired
    private WeatherService service;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/{weatherId}")
    public ResponseEntity<?> getWeather(@PathVariable UUID taskId, @PathVariable String weatherId) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.getWeather(taskId, userId, weatherId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getWeathers(@PathVariable UUID taskId) {
        UUID userId = securityUtil.getUserId();
        try {
            return ResponseEntity.ok(service.getAllWeathers(taskId, userId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }
}