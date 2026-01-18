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
public class UserTaskId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "task_id")
    private UUID taskId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTaskId that = (UserTaskId) o;
        return userId == that.userId &&
                taskId == that.taskId;
    }
    @Override
    public int hashCode() {
        return Objects.hash(userId, taskId);
    }
}