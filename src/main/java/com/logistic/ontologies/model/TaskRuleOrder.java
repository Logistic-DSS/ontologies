package com.logistic.ontologies.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "tasks_rules_order",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"task_id", "order_number"}
    )
)
@Getter
@NoArgsConstructor
public class TaskRuleOrder {

    @EmbeddedId
    private TaskRuleOrderId id;

    @Column(name = "order_number", nullable = false)
    private int orderNumber;
}