package com.esmpf.identity.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PermissionRepository extends JpaRepository<PermissionEntity, String> {}

interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    List<RolePermission> findAllByRoleId(UUID roleId);
    void deleteAllByRoleId(UUID roleId);
}

interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UUID> {
    Optional<UserRoleAssignment> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<UserRoleAssignment> findAllByBusinessIdAndUserId(UUID businessId, UUID userId, Pageable pageable);
    boolean existsByBusinessIdAndUserIdAndRoleIdAndStatus(UUID businessId, UUID userId, UUID roleId, String status);

    @Query("""
        select a from UserRoleAssignment a
        where a.userId=:userId and a.businessId=:businessId and a.status='ACTIVE'
          and (a.validFrom is null or a.validFrom<=:now)
          and (a.validUntil is null or a.validUntil>:now)
        """)
    List<UserRoleAssignment> findEffective(@Param("businessId") UUID businessId,
                                           @Param("userId") UUID userId,
                                           @Param("now") Instant now);

    @Query("""
        select count(a) from UserRoleAssignment a
        where a.businessId=:businessId and a.roleId=:roleId and a.status='ACTIVE'
          and (a.validFrom is null or a.validFrom<=:now)
          and (a.validUntil is null or a.validUntil>:now)
        """)
    long countEffectiveRoleAssignments(@Param("businessId") UUID businessId,
                                       @Param("roleId") UUID roleId,
                                       @Param("now") Instant now);
}
