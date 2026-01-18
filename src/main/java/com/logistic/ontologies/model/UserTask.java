package com.logistic.ontologies.model;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users_tasks")
@Getter
@NoArgsConstructor
public class UserTask {

    @EmbeddedId
    private UserTaskId id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private UserRole role;

    public UserTask(UUID userId, UUID taskId, UserRole role) {
        this.id = new UserTaskId(userId, taskId);
        this.role = role;
    }

    public UserTask(UUID userId, UUID taskId) {
        this.id = new UserTaskId(userId, taskId);
        this.role = UserRole.OWNER;
    }
}