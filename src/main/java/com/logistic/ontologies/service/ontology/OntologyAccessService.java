package com.logistic.ontologies.service.ontology;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistic.ontologies.dto.ontology.PermissionDTO;
import com.logistic.ontologies.model.OntologyType;
import com.logistic.ontologies.model.RuleOntology;
import com.logistic.ontologies.model.TaskOntology;
import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserRule;
import com.logistic.ontologies.model.UserTask;
import com.logistic.ontologies.repository.RuleOntologyRepository;
import com.logistic.ontologies.repository.TaskOntologyRepository;
import com.logistic.ontologies.repository.UserRuleRoleRepository;
import com.logistic.ontologies.repository.UserTaskRoleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OntologyAccessService {
    @Autowired
    private TaskOntologyRepository taskRepo;
    @Autowired
    private RuleOntologyRepository ruleRepo;
    @Autowired
    private UserTaskRoleRepository taskRoleRepo;
    @Autowired
    private UserRuleRoleRepository ruleRoleRepo;

    @Transactional
    private void checkTask(UUID taskId, UUID ownerId, UUID userId, UserRole roleType) {
        TaskOntology task = taskRepo.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task ontology not found"));
        
        if (task.getOwnerId() != ownerId)
            throw new AccessDeniedException("Only owner can grant permissions");

        if (userId == ownerId)
            throw new AccessDeniedException("Owner can't edit their permissions");

        if (roleType == UserRole.OWNER)
            throw new IllegalStateException("Ontology can have only one owner");
    }

    @Transactional
    private void checkRule(UUID ruleId, UUID ownerId, UUID userId, UserRole roleType) {
        RuleOntology rule = ruleRepo.findById(ruleId)
            .orElseThrow(() -> new EntityNotFoundException("Rule ontology not found"));
        
        if (rule.getOwnerId() != ownerId)
            throw new AccessDeniedException("Only owner can grant permissions");

        if (userId == ownerId)
            throw new AccessDeniedException("Owner can't edit their permissions");

        if (roleType == UserRole.OWNER)
            throw new IllegalStateException("Ontology can have only one owner");
    }

    @Transactional
    public void grant(UUID userId, PermissionDTO dto) {
        UUID newUserId = dto.userId();
        UserRole roleType = dto.userRole();
        if (dto.ontologyType() == OntologyType.TASK) {
            UUID taskOntologyId = dto.ontologyId();

            checkTask(taskOntologyId, userId, newUserId, roleType);

            UserTask role = taskRoleRepo.findByIdUserIdAndIdTaskId(newUserId, taskOntologyId).get();

            if (role != null) {
                if (role.getRole() == roleType) return;
                taskRoleRepo.delete(role);
                taskRoleRepo.save(new UserTask(newUserId, taskOntologyId, roleType));
            } else taskRoleRepo.save(new UserTask(newUserId, taskOntologyId, roleType));

        } else if (dto.ontologyType() == OntologyType.RULE) {
            UUID ruleOntologyId = dto.ontologyId();
        
            checkRule(ruleOntologyId, userId, newUserId, roleType);

            UserRule role = ruleRoleRepo.findByIdUserIdAndIdRuleId(newUserId, ruleOntologyId).get();

            if (role != null) {
                if (role.getRole() == roleType) return;
                ruleRoleRepo.delete(role);
                ruleRoleRepo.save(new UserRule(newUserId, ruleOntologyId, roleType));
            } else ruleRoleRepo.save(new UserRule(newUserId, ruleOntologyId, roleType));
        } else throw new IllegalArgumentException("Incorrect ontology type");
    }

    @Transactional
    public void withdraw(UUID userId, PermissionDTO dto) {
        UUID newUserId = dto.userId();
        UserRole roleType = dto.userRole();
        if (dto.ontologyType() == OntologyType.TASK) {
            UUID taskOntologyId = dto.ontologyId();
            
            checkTask(taskOntologyId, userId, newUserId, roleType);

            UserTask role = taskRoleRepo.findByIdUserIdAndIdTaskId(newUserId, taskOntologyId)
                .orElseThrow(() -> new EntityNotFoundException("User doesn't have pemissions for ontology"));
            
            taskRoleRepo.delete(role);

        } else if (dto.ontologyType() == OntologyType.RULE) {
            UUID ruleOntologyId = dto.ontologyId();
            
            checkRule(ruleOntologyId, userId, newUserId, roleType);

            UserRule role = ruleRoleRepo.findByIdUserIdAndIdRuleId(newUserId, ruleOntologyId)
                .orElseThrow(() -> new EntityNotFoundException("User doesn't have pemissions for ontology"));
            
            ruleRoleRepo.delete(role);
        } else throw new IllegalArgumentException("Incorrect ontology type");
    }

    @Transactional
    public List<PermissionDTO> getAccess(UUID userId, UUID ontologyId, OntologyType type) {
        if (type == OntologyType.TASK) {
            taskRepo.findById(ontologyId)
                .orElseThrow(() -> new EntityNotFoundException("Task ontology not found"));

            taskRoleRepo.findByIdUserIdAndIdTaskId(userId, ontologyId)
                .orElseThrow(() -> new AccessDeniedException("User doesn't have access for task ontology"));
            
            List<UserTask> usersTask = taskRoleRepo.findAllById(ontologyId);

            return usersTask
                .stream()
                .map(row -> new PermissionDTO(
                    type,
                    ontologyId,
                    row.getId().getUserId(),
                    row.getRole()
                ))
                .toList();
        } else if (type == OntologyType.RULE) {
            ruleRepo.findById(ontologyId)
                .orElseThrow(() -> new EntityNotFoundException("Rule ontology not found"));
            
            ruleRoleRepo.findByIdUserIdAndIdRuleId(userId, ontologyId)
                .orElseThrow(() -> new AccessDeniedException("User doesn't have access for task ontology"));
            
            List<UserRule> usersRule = ruleRoleRepo.findAllById(ontologyId);

            return usersRule.stream().map(
                (row) -> {
                    return new PermissionDTO(
                        type,
                        ontologyId,
                        row.getId().getUserId(),
                        row.getRole()
                    );
                }
            ).toList();
        } else throw new IllegalArgumentException("Incorrect ontology type");
    }
}
