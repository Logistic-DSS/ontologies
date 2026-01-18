package com.logistic.ontologies.service.ontology;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistic.ontologies.dto.ontology.OntologyCreateDTO;
import com.logistic.ontologies.dto.ontology.OntologyDTO;
import com.logistic.ontologies.model.TaskOntology;
import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserTask;
import com.logistic.ontologies.repository.TaskOntologyRepository;
import com.logistic.ontologies.repository.UserTaskRoleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TaskOntologyService {
    @Autowired
    private TaskOntologyRepository taskRepo;
    @Autowired
    private UserTaskRoleRepository roleRepo;
    @Autowired
    private OntologyStorage ontologyStorage;

    @Transactional
    public OntologyDTO createTaskOntology(UUID ownerId, OntologyCreateDTO dto) throws OWLOntologyCreationException {
        TaskOntology task = new TaskOntology();
        task.setOwnerId(ownerId);
        task.setName(dto.name());
        task.setDescription(dto.description());

        taskRepo.save(task);

        UserTask role = new UserTask(task.getOwnerId(), task.getId());

        roleRepo.save(role);

        ontologyStorage.createTaskOntology(task.getId());

        return new OntologyDTO(
            task.getId(),
            task.getOwnerId(),
            task.getName(),
            task.getDescription(),
            Date.from(task.getCreatedAt()),
            Date.from(task.getUpdatedAt())
        );
    }

    @Transactional(readOnly = true)
    public OntologyDTO getTaskOntology(UUID taskId, UUID userId) {
        TaskOntology task = taskRepo.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));
        
        return new OntologyDTO(
            task.getId(),
            task.getOwnerId(),
            task.getName(),
            task.getDescription(),
            Date.from(task.getCreatedAt()),
            Date.from(task.getUpdatedAt())
        );
    }

    @Transactional(readOnly = true)
    public List<OntologyDTO> getAllTaskOntologies(UUID userId) {
        List<UserTask> tasks = roleRepo.findAllByIdUserId(userId);

        ArrayList<OntologyDTO> res = new ArrayList<OntologyDTO>();
        for (UserTask task : tasks) {
            UUID taskId = task.getId().getTaskId();
            res.add(getTaskOntology(taskId, userId));
        }

        return res;
    }

    @Transactional
    public void deleteTaskOntology(UUID taskId, UUID userId) throws IOException {
        taskRepo.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() != UserRole.OWNER)
            throw new AccessDeniedException("Only owner can delete task");

        ontologyStorage.deleteTaskOntology(taskId);
        taskRepo.deleteById(taskId);
    }

    @Transactional
    public void updateTaskOntology(UUID taskId, UUID userId, OntologyCreateDTO dto) {
        TaskOntology task = taskRepo.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        UserTask role = roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (role.getRole() == UserRole.READER)
            throw new AccessDeniedException("Reader can't edit task properties");

        task.setName(dto.name());
        task.setDescription(dto.description());
        task.setUpdatedAt(Instant.now());
        taskRepo.save(task);
    }
}