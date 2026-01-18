package com.logistic.ontologies.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistic.ontologies.model.TaskRuleOrder;
import com.logistic.ontologies.model.TaskRuleOrderId;

public interface TaskRuleOrderRepository
        extends JpaRepository<TaskRuleOrder, TaskRuleOrderId> {

    List<TaskRuleOrder> findAllByIdTaskId(UUID taskId);

    boolean existsByIdTaskIdAndIdRuleId(UUID taskId, UUID ruleId);

    void deleteAllByIdTaskId(UUID taskId);
}