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

import com.logistic.ontologies.dto.domain.CargoCreateDTO;
import com.logistic.ontologies.dto.domain.CargoDTO;
import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserTask;
import com.logistic.ontologies.repository.UserTaskRoleRepository;

@Service
public class CargoService {
    @Autowired
    private UserTaskRoleRepository roleRepo;
    @Autowired
    private BaseDomainService helpers;


    @Transactional
    public CargoDTO createCargo(UUID taskId, UUID userId, CargoCreateDTO dto)
            throws OWLOntologyCreationException, OWLOntologyStorageException {
        
        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't create individuals");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        UUID individID = UUID.randomUUID();
        OWLNamedIndividual cargo = helpers.createIndividual(task, "Груз", individID);
        helpers.setDataProperty(task, cargo, "Название_груза", dto.name());
        helpers.setDataProperty(task, cargo, "Вес", dto.weight());
        helpers.save(task);

        return new CargoDTO(
            taskId,
            individID,
            dto.name(),
            dto.weight()
        );
    }

    @Transactional
    public void updateCargo(UUID taskId, UUID userId, UUID cargoId, CargoCreateDTO dto)
            throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't edit individuals");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual cargo = helpers.requireIndividual(task, "Груз", cargoId);
        helpers.setDataProperty(task, cargo, "Название_груза", dto.name());
        helpers.setDataProperty(task, cargo, "Вес", dto.weight());
        helpers.save(task);
    }

    @Transactional
    public void deleteCargo(UUID taskId, UUID userId, UUID cargoId)
            throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual cargo = helpers.requireIndividual(task, "Груз", cargoId);
        helpers.deleteIndividual(task, cargo);
        helpers.save(task);
    }

    @Transactional(readOnly = true)
    public CargoDTO getCargo(UUID taskId, UUID userId, UUID cargoId)
            throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));
        
        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual cargo = helpers.requireIndividual(task, "Груз", cargoId);
        String name = helpers.getDataPropertyValue(task, cargo, "Название_груза", String.class);
        Integer weight = helpers.getDataPropertyValue(task, cargo, "Вес", Integer.class);

        return new CargoDTO(
            taskId,
            cargoId,
            name,
            weight
        );
    }

    @Transactional(readOnly = true)
    public List<CargoDTO> getAllCargos(UUID taskId, UUID userId)
            throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology task = helpers.loadTaskOntology(taskId);

        return helpers.requireAllIndividuals(task, "Груз")
            .stream()
            .map(ind -> {
                return new CargoDTO(
                    taskId,
                    helpers.getIDFromURI(ind.getIRI()),
                    helpers.getDataPropertyValue(task, ind, "Название_груза", String.class),
                    helpers.getDataPropertyValue(task, ind, "Вес", Integer.class)
                );
            })
            .toList();
    }
}