package com.logistic.ontologies.service.reasoning;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistic.ontologies.dto.reasoning.OrderDTO;
import com.logistic.ontologies.model.TaskRuleOrder;
import com.logistic.ontologies.model.TaskRuleOrderId;
import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserRule;
import com.logistic.ontologies.model.UserTask;
import com.logistic.ontologies.repository.RuleOntologyRepository;
import com.logistic.ontologies.repository.TaskOntologyRepository;
import com.logistic.ontologies.repository.TaskRuleOrderRepository;
import com.logistic.ontologies.repository.UserRuleRoleRepository;
import com.logistic.ontologies.repository.UserTaskRoleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReasoningConfigurationService {
    @Autowired
    private TaskOntologyRepository taskRepo;
    @Autowired
    private RuleOntologyRepository ruleRepo;
    @Autowired
    private UserTaskRoleRepository taskRoleRepo;
    @Autowired
    private UserRuleRoleRepository ruleRoleRepo;
    @Autowired
    private TaskRuleOrderRepository taskRuleRepo;

    private void checkRole(UUID taskOntologyId, UUID ruleOntologyId, UUID userId) {
        taskRepo.findById(taskOntologyId)
            .orElseThrow(() -> new EntityNotFoundException("Task ontology not found"));
        
        UserTask taskRole = taskRoleRepo.findByIdUserIdAndIdTaskId(userId, taskOntologyId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (taskRole.getRole() != UserRole.OWNER)
            throw new AccessDeniedException("Only owner can manage task bindings");

        ruleRepo.findById(ruleOntologyId)
            .orElseThrow(() -> new EntityNotFoundException("Rule ontology not found"));

        UserRule ruleRole = ruleRoleRepo.findByIdUserIdAndIdRuleId(userId, ruleOntologyId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        if (ruleRole.getRole() != UserRole.OWNER)
            throw new AccessDeniedException("Only owner can manage rule bindings");
    }

    @Transactional
    public void bind(UUID userId, OrderDTO dto) {
        UUID taskOntologyId = dto.taskOnologyId();
        UUID ruleOntologyId = dto.ruleOntologyId();
        Integer order = dto.orderNumber();
        
        checkRole(taskOntologyId, ruleOntologyId, userId);
        TaskRuleOrderId id = new TaskRuleOrderId(taskOntologyId, ruleOntologyId);
        if (taskRuleRepo.existsById(id))
            throw new IllegalStateException("Rule ontology already bound");

        int count = taskRuleRepo.countByIdTaskId(taskOntologyId);

        validateOrder(order, count + 1);
        shiftRight(taskOntologyId, order);

        TaskRuleOrder entity = new TaskRuleOrder();
        entity.setId(id);
        entity.setOrderNumber(order);

        taskRuleRepo.save(entity);
    }

    @Transactional
    public void unbind(UUID userId, UUID taskOntologyId, UUID ruleOntologyId) {
        checkRole(taskOntologyId, ruleOntologyId, userId);

        TaskRuleOrder entity = taskRuleRepo.findById(new TaskRuleOrderId(taskOntologyId, ruleOntologyId))
            .orElseThrow(() -> new EntityNotFoundException("Rule ontology isn't bind"));

        Integer removedOrder = entity.getOrderNumber();

        taskRuleRepo.delete(entity);

        List<TaskRuleOrder> affected = taskRuleRepo.findByIdTaskIdAndOrderNumberGreaterThan(
            taskOntologyId,
            removedOrder
        );

        for (TaskRuleOrder item : affected) item.setOrderNumber(item.getOrderNumber() - 1);
    }

    @Transactional
    // public void changeOrder(UUID userId, OrderDTO dto) {
    public void changeOrder(UUID userId, UUID taskOntologyId, UUID ruleOntologyId, Integer order) {
        // UUID taskOntologyId = dto.taskOnologyId();
        // UUID ruleOntologyId = dto.ruleOntologyId();
        // Integer order = dto.orderNumber();
        checkRole(taskOntologyId, ruleOntologyId, userId);

        TaskRuleOrder entity = taskRuleRepo.findById(new TaskRuleOrderId(taskOntologyId, ruleOntologyId))
            .orElseThrow(() -> new EntityNotFoundException("Rule ontology isn't bind"));
        
        int count = taskRuleRepo.countByIdTaskId(taskOntologyId);
        validateOrder(order, count);

        int oldOrder = entity.getOrderNumber();
        if (oldOrder == order) return;

        if (order < oldOrder) {
            List<TaskRuleOrder> affected = taskRuleRepo.findByIdTaskIdAndOrderNumberBetween(
                taskOntologyId,
                order,
                oldOrder - 1
            );
            for (TaskRuleOrder item : affected) item.setOrderNumber(item.getOrderNumber() + 1);
        } else {
            List<TaskRuleOrder> affected = taskRuleRepo.findByIdTaskIdAndOrderNumberBetween(
                taskOntologyId,
                oldOrder + 1,
                order
            );
            for (TaskRuleOrder item : affected) item.setOrderNumber(item.getOrderNumber() - 1);
        }
        entity.setOrderNumber(order);
    }

    private void validateOrder(int order, int maxAllowed) {
        if (order < 1 || order > maxAllowed)
            throw new IllegalArgumentException("Order must be between 1 and " + maxAllowed);
    }

    private void shiftRight(UUID taskOntologyId, int fromOrder) {
        List<TaskRuleOrder> affected = taskRuleRepo.findByIdTaskIdAndOrderNumberGreaterThan(
            taskOntologyId,
            fromOrder-1
        );

        for (TaskRuleOrder item : affected) item.setOrderNumber(item.getOrderNumber() + 1);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrderedRules(UUID taskOntologyId, UUID userId) {
         taskRepo.findById(taskOntologyId)
            .orElseThrow(() -> new EntityNotFoundException("Task ontology not found"));
        
        taskRoleRepo.findByIdUserIdAndIdTaskId(userId, taskOntologyId)
            .orElseThrow(() -> new AccessDeniedException("No access"));
        
        return taskRuleRepo.findByIdTaskIdOrderByOrderNumberAsc(taskOntologyId)
            .stream()
            .map(x -> new OrderDTO(
                x.getId().getTaskId(),
                x.getId().getRuleId(),
                x.getOrderNumber()
            ))
            .toList();
    }
}
