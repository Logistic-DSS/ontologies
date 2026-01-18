package com.logistic.ontologies.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.logistic.ontologies.model.TaskOntology;

public interface TaskOntologyRepository
        extends JpaRepository<TaskOntology, UUID> {

    Page<TaskOntology> findAllByOwnerId(UUID ownerId, Pageable pageable);

    Optional<TaskOntology> findByIdAndOwnerId(UUID id, UUID ownerId);

    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);
}