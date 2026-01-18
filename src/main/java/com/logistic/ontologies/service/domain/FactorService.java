package com.logistic.ontologies.service.domain;

import java.util.List;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistic.ontologies.dto.domain.FactorCreateDTO;
import com.logistic.ontologies.dto.domain.FactorDTO;
import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserTask;
import com.logistic.ontologies.repository.UserTaskRoleRepository;

@Service
public class FactorService {
    @Autowired
    private UserTaskRoleRepository roleRepo;
    @Autowired
    private BaseDomainService helpers;


    @Transactional
    public FactorDTO createFactor(UUID taskId, UUID userId, FactorCreateDTO dto)
            throws OWLOntologyCreationException, OWLOntologyStorageException {
        
        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't create individuals");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        UUID individID = UUID.randomUUID();
        OWLNamedIndividual factor = helpers.createIndividual(task, "Фактор", individID);
        helpers.setDataProperty(task, factor, "Название_фактора", dto.factorName());
        helpers.setDataProperty(task, factor, "Влияние_фактора", dto.factorValue());
        helpers.save(task);

        return new FactorDTO(
            taskId,
            individID,
            dto.factorName(),
            dto.factorValue()
        );
    }

    @Transactional
    public void updateFactor(UUID taskId, UUID userId, UUID factorId, FactorCreateDTO dto)
            throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't edit individuals");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual factor = helpers.createIndividual(task, "Фактор", factorId);
        helpers.setDataProperty(task, factor, "Название_фактора", dto.factorName());
        helpers.setDataProperty(task, factor, "Влияние_фактора", dto.factorValue());
        helpers.save(task);
    }

    @Transactional
    public void deleteFactor(UUID taskId, UUID userId, UUID factorId)
            throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual factor = helpers.createIndividual(task, "Фактор", factorId);
        helpers.deleteIndividual(task, factor);
        helpers.save(task);
    }

    @Transactional(readOnly = true)
    public FactorDTO getFactor(UUID taskId, UUID userId, UUID factorId)
            throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual factor = helpers.createIndividual(task, "Фактор", factorId);
        String name = helpers.getDataPropertyValue(task, factor, "Название_фактора", String.class);
        Integer value = helpers.getDataPropertyValue(task, factor, "Влияние_фактора", Integer.class);

        return new FactorDTO(
            taskId,
            factorId,
            name,
            value
        );
    }

    @Transactional(readOnly = true)
    public List<FactorDTO> getAllFactors(UUID taskId, UUID userId)
            throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology task = helpers.loadTaskOntology(taskId);

        return helpers.requireAllIndividuals(task, "Фактор")
            .stream()
            .map(ind -> {
                return new FactorDTO(
                    taskId,
                    helpers.getIDFromURI(ind.getIRI()),
                    helpers.getDataPropertyValue(task, ind, "Название_фактора", String.class),
                    helpers.getDataPropertyValue(task, ind, "Влияние_фактора", Integer.class)
                );
            })
            .toList();
    }
}
