package com.logistic.ontologies.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserTask;
import com.logistic.ontologies.model.UserTaskId;

public interface UserTaskRoleRepository
        extends JpaRepository<UserTask, UserTaskId> {

    Optional<UserTask> findByIdUserIdAndIdTaskId(UUID userId, UUID taskId);

    List<UserTask> findAllByIdUserId(UUID userId);

    boolean existsByIdUserIdAndIdTaskIdAndRole(
        UUID userId,
        UUID taskId,
        UserRole role
    );
}