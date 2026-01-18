package com.logistic.ontologies.model;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskRuleOrderId implements Serializable {

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "rule_id")
    private UUID ruleId;
}