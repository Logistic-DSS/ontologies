package com.logistic.ontologies.model;

import java.io.Serializable;
import java.util.Objects;
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
public class UserRuleId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "rule_id")
    private UUID ruleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRuleId that = (UserRuleId) o;
        return userId == that.userId &&
                ruleId == that.ruleId;
    }
    @Override
    public int hashCode() {
        return Objects.hash(userId, ruleId);
    }
}