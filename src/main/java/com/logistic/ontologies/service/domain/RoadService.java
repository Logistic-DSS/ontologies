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

import com.logistic.ontologies.dto.domain.RoadCreateDTO;
import com.logistic.ontologies.dto.domain.RoadDTO;
import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserTask;
import com.logistic.ontologies.repository.UserTaskRoleRepository;

@Service
public class RoadService {
    @Autowired
    private UserTaskRoleRepository roleRepo;
    @Autowired
    private BaseDomainService helpers;


    @Transactional
    public RoadDTO createRoad(UUID taskId, UUID userId, RoadCreateDTO dto) throws OWLOntologyCreationException, OWLOntologyStorageException {
        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't create individuals");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        UUID individID = UUID.randomUUID();
        OWLNamedIndividual road = helpers.createIndividual(task, "Дорога", individID);
        helpers.setDataProperty(task, road, "Тип_дорожного_покрытия", dto.roadType());
        helpers.setDataProperty(task, road, "Протяженность_дорожного_покрытия", dto.length());
        helpers.setDataProperty(task, road, "Состояние_дорожного_покрытия", dto.coatingWear());
        helpers.save(task);

        return new RoadDTO(
            taskId,
            individID,
            dto.roadType(),
            dto.length(),
            dto.coatingWear(),
            "Ясно",
            null
        );
    }

    @Transactional
    public void updateRoad(UUID taskId, UUID userId, UUID roadId, RoadCreateDTO dto) throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't edit individuals");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual road = helpers.createIndividual(task, "Дорога", roadId);
        helpers.setDataProperty(task, road, "Тип_дорожного_покрытия", dto.roadType());
        helpers.setDataProperty(task, road, "Протяженность_дорожного_покрытия", dto.length());
        helpers.setDataProperty(task, road, "Состояние_дорожного_покрытия", dto.coatingWear());
        helpers.save(task);
    }

    @Transactional
    public void deleteRoad(UUID taskId, UUID userId, UUID roadId) throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() != UserRole.OWNER && role.getRole() != UserRole.EDITOR)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual road = helpers.createIndividual(task, "Дорога", roadId);
        helpers.deleteIndividual(task, road);
        helpers.save(task);
    }

    @Transactional
    public void assignWeather(UUID taskId, UUID userId, UUID roadId, String weatherId) throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual road = helpers.createIndividual(task, "Дорога", roadId);
        helpers.requireAllIndividuals(task, "Погода")
            .stream()
            .forEach(ind -> {
                helpers.deleteObjectProperty(task, road, "Стоит", ind);
            });
        OWLNamedIndividual weather = helpers.requireIndividual(task, "Погода", weatherId);
        helpers.addObjectProperty(task, road, "Стоит", weather);
        helpers.save(task);
    }

    @Transactional
    public void unassignWeather(UUID taskId, UUID userId, UUID roadId, String weatherId) throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual road = helpers.createIndividual(task, "Дорога", roadId);
        OWLNamedIndividual weather = helpers.requireIndividual(task, "Погода", weatherId);
        helpers.deleteObjectProperty(task, road, "Стоит", weather);
        helpers.save(task);
    }

    @Transactional
    public void assignFactor(UUID taskId, UUID userId, UUID roadId, UUID factorId) throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual road = helpers.createIndividual(task, "Дорога", roadId);
        OWLNamedIndividual factor = helpers.requireIndividual(task, "Фактор", factorId);
        helpers.addObjectProperty(task, road, "Имеет_фактор", factor);
        helpers.save(task);
    }

    @Transactional
    public void unassignFactor(UUID taskId, UUID userId, UUID roadId, UUID factorId) throws OWLOntologyCreationException, OWLOntologyStorageException {

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("No permission");

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual road = helpers.createIndividual(task, "Дорога", roadId);
        OWLNamedIndividual factor = helpers.requireIndividual(task, "Фактор", factorId);
        helpers.deleteObjectProperty(task, road, "Имеет_фактор", factor);
        helpers.save(task);
    }

    @Transactional(readOnly = true)
    public RoadDTO getRoad(UUID taskId, UUID userId, UUID roadId)
            throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual road = helpers.requireIndividual(task, "Дорога", roadId);
        String roadtype = helpers.getDataPropertyValue(task, road, "Тип_дорожного_покрытия", String.class);
        Integer length = helpers.getDataPropertyValue(task, road, "Протяженность_дорожного_покрытия", Integer.class);
        String coatingWear = helpers.getDataPropertyValue(task, road, "Состояние_дорожного_покрытия", String.class);
       
        List<UUID> factors = helpers.getObjectPropertyValues(task, road, "Имеет_фактор")
            .stream()
            .map(factor -> { return helpers.getIDFromURI(factor.getIRI()); })
            .toList();

        OWLNamedIndividual weather = helpers.getObjectPropertyValues(task, road, "Стоит_погода")
            .stream()
            .findFirst().orElse(null);
        
        String weatherId = weather == null ? helpers.getStrIDFromURI(weather.getIRI()) : null;

        return new RoadDTO(
            taskId,
            roadId,
            roadtype,
            length,
            coatingWear,
            weatherId,
            factors
        );
    }

    @Transactional(readOnly = true)
    public List<RoadDTO> getAllRoads(UUID taskId, UUID userId) throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology task = helpers.loadTaskOntology(taskId);

        return helpers.requireAllIndividuals(task, "Дорога")
            .stream()
            .map(ind -> {
                String roadtype = helpers.getDataPropertyValue(task, ind, "Тип_дорожного_покрытия", String.class);
                Integer length = helpers.getDataPropertyValue(task, ind, "Протяженность_дорожного_покрытия", Integer.class);
                String coatingWear = helpers.getDataPropertyValue(task, ind, "Состояние_дорожного_покрытия", String.class);
            
                List<UUID> factors = helpers.getObjectPropertyValues(task, ind, "Имеет_фактор")
                    .stream()
                    .map(factor -> { return helpers.getIDFromURI(factor.getIRI()); })
                    .toList();

                OWLNamedIndividual weather = helpers.getObjectPropertyValues(task, ind, "Стоит_погода")
                    .stream()
                    .findFirst().orElse(null);
                
                String weatherId = weather == null ? helpers.getStrIDFromURI(weather.getIRI()) : null;

                return new RoadDTO(
                    taskId,
                    helpers.getIDFromURI(ind.getIRI()),
                    roadtype,
                    length,
                    coatingWear,
                    weatherId,
                    factors
                );
            })
            .toList();
    }
}
