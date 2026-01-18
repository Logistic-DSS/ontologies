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
@Table(name = "users_rules")
@Getter
@NoArgsConstructor
public class UserRule {

    @EmbeddedId
    private UserRuleId id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private UserRole role;

    public UserRule(UUID userId, UUID taskId, UserRole role) {
        this.id = new UserRuleId(userId, taskId);
        this.role = role;
    }

    public UserRule(UUID userId, UUID taskId) {
        this.id = new UserRuleId(userId, taskId);
        this.role = UserRole.OWNER;
    }
}