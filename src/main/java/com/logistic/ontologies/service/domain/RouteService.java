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

import com.logistic.ontologies.dto.domain.RouteCreateDTO;
import com.logistic.ontologies.dto.domain.RouteDTO;
import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserTask;
import com.logistic.ontologies.repository.UserTaskRoleRepository;

@Service
public class RouteService {
    @Autowired
    private UserTaskRoleRepository roleRepo;
    @Autowired
    private BaseDomainService helpers;


    @Transactional
    public RouteDTO createRoute(UUID taskId, UUID userId, RouteCreateDTO dto) throws OWLOntologyCreationException, OWLOntologyStorageException {
        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't create individuals");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        UUID individID = UUID.randomUUID();
        OWLNamedIndividual route = helpers.createIndividual(task, "Маршрут", individID);
        for (UUID roadId : dto.roads()) assignRoad(taskId, userId, individID, roadId);
        helpers.save(task);

        return new RouteDTO(
            taskId,
            individID,
            dto.roads()
        );
    }

    @Transactional
    public void deleteRoute(UUID taskId, UUID userId, UUID routeId) throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() != UserRole.OWNER && role.getRole() != UserRole.EDITOR)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual route = helpers.requireIndividual(task, "Маршрут", routeId);
        helpers.deleteIndividual(task, route);
        helpers.save(task);
    }

    @Transactional
    public void assignRoad(UUID taskId, UUID userId, UUID routeId, UUID roadId) throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual route = helpers.requireIndividual(task, "Маршрут", routeId);
        OWLNamedIndividual road = helpers.requireIndividual(task, "Дорога", roadId);
        helpers.addObjectProperty(task, route, "Состоит_из", road);
        helpers.save(task);
    }

    @Transactional
    public void unassignRoad(UUID taskId, UUID userId, UUID routeId, UUID roadId) throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual route = helpers.requireIndividual(task, "Маршрут", routeId);
        OWLNamedIndividual road = helpers.requireIndividual(task, "Дорога", roadId);
        helpers.deleteObjectProperty(task, route, "Состоит_из", road);
        helpers.save(task);
    }

    @Transactional(readOnly = true)
    public RouteDTO getRoute(UUID taskId, UUID userId, UUID routeId) throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual route = helpers.requireIndividual(task, "Маршрут", routeId);
        List<UUID> roads = helpers.getObjectPropertyValues(task, route, "Состоит_из")
            .stream()
            .map(road -> { return helpers.getIDFromURI(road.getIRI()); })
            .toList();

        return new RouteDTO(
            taskId,
            routeId,
            roads
        );
    }

    @Transactional(readOnly = true)
    public List<RouteDTO> getAllRoutes(UUID taskId, UUID userId) throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));


        OWLOntology task = helpers.loadTaskOntology(taskId);
        return helpers.requireAllIndividuals(task, "Маршрут")
            .stream()
            .map(route -> {
                List<UUID> roads = helpers.getObjectPropertyValues(task, route, "Состоит_из")
                .stream()
                .map(road -> { return helpers.getIDFromURI(road.getIRI()); })
                .toList();

                return new RouteDTO(
                    taskId,
                    helpers.getIDFromURI(route.getIRI()),
                    roads
                );
            })
            .toList();
    }
}
