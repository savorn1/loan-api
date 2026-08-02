package com.example.loan.repository;

import com.example.loan.entity.GroupMember;
import com.example.loan.entity.GroupMemberRole;
import com.example.loan.entity.GroupMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupIdOrderByJoinedAtAsc(Long groupId);

    Optional<GroupMember> findByGroupIdAndCustomerIdAndStatus(Long groupId, Long customerId, GroupMemberStatus status);

    boolean existsByGroupIdAndCustomerIdAndStatus(Long groupId, Long customerId, GroupMemberStatus status);

    long countByGroupIdAndStatus(Long groupId, GroupMemberStatus status);

    Optional<GroupMember> findFirstByGroupIdAndRoleAndStatus(Long groupId, GroupMemberRole role, GroupMemberStatus status);
}
