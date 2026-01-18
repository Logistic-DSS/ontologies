package com.logistic.ontologies.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistic.ontologies.model.UserRole;
import com.logistic.ontologies.model.UserRule;
import com.logistic.ontologies.model.UserRuleId;

public interface UserRuleRoleRepository
        extends JpaRepository<UserRule, UserRuleId> {

    Optional<UserRule> findByIdUserIdAndIdRuleId(UUID userId, UUID ruleId);

    List<UserRule> findAllByIdUserId(UUID userId);

    boolean existsByIdUserIdAndIdRuleIdAndRole(
        UUID userId,
        UUID ruleId,
        UserRole role
    );
}