package com.logistic.ontologies.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.logistic.ontologies.model.RuleOntology;

public interface RuleOntologyRepository
        extends JpaRepository<RuleOntology, UUID> {

    Page<RuleOntology> findAllByOwnerId(UUID ownerId, Pageable pageable);

    Optional<RuleOntology> findByIdAndOwnerId(UUID id, UUID ownerId);
}