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

import com.logistic.ontologies.dto.domain.TransportCreateDTO;
import com.logistic.ontologies.dto.domain.TransportDTO;
import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserTask;
import com.logistic.ontologies.repository.UserTaskRoleRepository;

@Service
public class TransportService {
    @Autowired
    private UserTaskRoleRepository roleRepo;
    @Autowired
    private BaseDomainService helpers;


    @Transactional
    public TransportDTO createTransport(UUID taskId, UUID userId, TransportCreateDTO dto)
            throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't create individuals");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        UUID individID = UUID.randomUUID();
        OWLNamedIndividual transport = helpers.createIndividual(task, "Транспорт", individID);
        helpers.setDataProperty(task, transport, "Название_транспорта", dto.name());
        helpers.setDataProperty(task, transport, "Максимальная_скорость", dto.maxSpeed());
        helpers.setDataProperty(task, transport, "Грузоподъемность", dto.payload());
        helpers.save(task);

        return new TransportDTO(
            taskId,
            individID,
            dto.name(),
            dto.maxSpeed(),
            dto.payload(),
            null
        );
    }

    @Transactional
    public void updateTransport(UUID taskId, UUID userId, UUID transportId, TransportCreateDTO dto)
            throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't edit individuals");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual transport = helpers.requireIndividual(task, "Транспорт", transportId);
        helpers.setDataProperty(task, transport, "Название_транспорта", dto.name());
        helpers.setDataProperty(task, transport, "Максимальная_скорость", dto.maxSpeed());
        helpers.setDataProperty(task, transport, "Грузоподъемность", dto.payload());
        helpers.save(task);
    }

    @Transactional
    public void deleteTransport(UUID taskId, UUID userId, UUID transportId)
            throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() != UserRole.OWNER && role.getRole() != UserRole.EDITOR)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual transport = helpers.requireIndividual(task, "Транспорт", transportId);
        helpers.deleteIndividual(task, transport);
        helpers.save(task);
    }

    @Transactional
    public void assignCargo(UUID taskId, UUID userId, UUID transportId, UUID cargoId)
            throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual transport = helpers.requireIndividual(task, "Транспорт", transportId);
        OWLNamedIndividual cargo = helpers.requireIndividual(task, "Груз", cargoId);
        helpers.addObjectProperty(task, transport, "Перевозит", cargo);
        helpers.save(task);
    }

    @Transactional
    public void unassignCargo(UUID taskId, UUID userId, UUID transportId, UUID cargoId)
            throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual transport = helpers.requireIndividual(task, "Транспорт", transportId);
        OWLNamedIndividual cargo = helpers.requireIndividual(task, "Груз", cargoId);
        helpers.deleteObjectProperty(task, transport, "Перевозит", cargo);
        helpers.save(task);
    }

    @Transactional(readOnly = true)
    public TransportDTO getTransport(UUID taskId, UUID userId, UUID transportId)
            throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual transport = helpers.requireIndividual(task, "Транспорт", transportId);
        String name = helpers.getDataPropertyValue(task, transport, "Название_транспорта", String.class);
        Integer maxSpeed = helpers.getDataPropertyValue(task, transport, "Максимальная_скорость", Integer.class);
        Integer payload = helpers.getDataPropertyValue(task, transport, "Грузоподъемность", Integer.class);
       
        List<UUID> cargos = helpers.getObjectPropertyValues(task, transport, "Перевозит")
            .stream()
            .map(cargo -> { return helpers.getIDFromURI(cargo.getIRI()); })
            .toList();

        return new TransportDTO(
            taskId,
            transportId,
            name,
            maxSpeed,
            payload,
            cargos
        );
    }

    @Transactional(readOnly = true)
    public List<TransportDTO> getAllTransports(UUID taskId, UUID userId)
            throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology task = helpers.loadTaskOntology(taskId);

        return helpers.requireAllIndividuals(task, "Транспорт")
            .stream()
            .map(transport -> {
                String name = helpers.getDataPropertyValue(task, transport, "Название_транспорта", String.class);
                Integer maxSpeed = helpers.getDataPropertyValue(task, transport, "Максимальная_скорость", Integer.class);
                Integer payload = helpers.getDataPropertyValue(task, transport, "Грузоподъемность", Integer.class);
                List<UUID> cargos = helpers.getObjectPropertyValues(task, transport, "Перевозит")
                .stream()
                .map(cargo -> { return helpers.getIDFromURI(cargo.getIRI()); })
                .toList();

                return new TransportDTO(
                    taskId,
                    helpers.getIDFromURI(transport.getIRI()),
                    name,
                    maxSpeed,
                    payload,
                    cargos
                );
            })
            .toList();
    }
}
