package com.example.loan.service.impl;

import com.example.loan.client.BranchClient;
import com.example.loan.client.CustomerClient;
import com.example.loan.client.LoanProductClient;
import com.example.loan.common.PageResponse;
import com.example.loan.dto.AddGroupMemberRequest;
import com.example.loan.dto.CustomerIdentityResponse;
import com.example.loan.dto.CustomerResponse;
import com.example.loan.dto.GroupFilterRequest;
import com.example.loan.dto.GroupMemberResponse;
import com.example.loan.dto.GroupRequest;
import com.example.loan.dto.GroupResponse;
import com.example.loan.dto.GroupStatusUpdateRequest;
import com.example.loan.dto.SetGroupLeaderRequest;
import com.example.loan.entity.Group;
import com.example.loan.entity.GroupMember;
import com.example.loan.entity.GroupMemberRole;
import com.example.loan.entity.GroupMemberStatus;
import com.example.loan.entity.GroupStatus;
import com.example.loan.exception.AppException;
import com.example.loan.exception.ResourceNotFoundException;
import com.example.loan.repository.GroupMemberRepository;
import com.example.loan.repository.GroupRepository;
import com.example.loan.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CustomerClient customerClient;
    private final BranchClient branchClient;
    private final LoanProductClient loanProductClient;

    @Override
    public GroupResponse create(GroupRequest request) {
        Group group = Group.builder()
                .name(request.getName())
                .code(request.getCode())
                .loanProductId(request.getLoanProductId())
                .branchId(request.getBranchId())
                .leaderCustomerId(request.getLeaderCustomerId())
                .formationDate(request.getFormationDate())
                .status(GroupStatus.ACTIVE)
                .build();
        group = groupRepository.save(group);

        if (group.getCode() == null || group.getCode().isBlank()) {
            group.setCode("GRP" + String.format("%06d", group.getId()));
            group = groupRepository.save(group);
        }

        return toResponse(group);
    }

    @Override
    public GroupResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public PageResponse<GroupResponse> list(GroupFilterRequest filter) {
        List<Specification<Group>> conditions = new ArrayList<>();

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String like = "%" + filter.getSearch().toLowerCase() + "%";
            conditions.add((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("code")), like)));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getBranchId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("branchId"), filter.getBranchId()));
        }

        Specification<Group> spec = Specification.allOf(conditions);

        Sort sort = "asc".equalsIgnoreCase(filter.getSortOrder())
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        Pageable pageable = PageRequest.of(Math.max(filter.getPage() - 1, 0), filter.getSize(), sort);

        Page<GroupResponse> page = groupRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.of(page);
    }

    @Override
    public GroupResponse update(Long id, GroupRequest request) {
        Group group = findOrThrow(id);
        if (group.getStatus() == GroupStatus.CLOSED) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot edit a closed group");
        }
        group.setName(request.getName());
        if (request.getCode() != null && !request.getCode().isBlank()) {
            group.setCode(request.getCode());
        }
        group.setLoanProductId(request.getLoanProductId());
        group.setBranchId(request.getBranchId());
        group.setLeaderCustomerId(request.getLeaderCustomerId());
        group.setFormationDate(request.getFormationDate());
        return toResponse(groupRepository.save(group));
    }

    @Override
    public GroupResponse updateStatus(Long id, GroupStatusUpdateRequest request) {
        Group group = findOrThrow(id);
        if (group.getStatus() == GroupStatus.CLOSED) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot change status of a closed group");
        }
        if (request.getStatus() == GroupStatus.CLOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Use the close endpoint to close a group");
        }
        group.setStatus(request.getStatus());
        return toResponse(groupRepository.save(group));
    }

    @Override
    @Transactional
    public GroupResponse setLeader(Long id, SetGroupLeaderRequest request) {
        Group group = findOrThrow(id);
        if (group.getStatus() == GroupStatus.CLOSED) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot change the leader of a closed group");
        }
        GroupMember newLeader = groupMemberRepository
                .findByGroupIdAndCustomerIdAndStatus(id, request.getCustomerId(), GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                        "Customer must be an active member of the group to become leader"));

        groupMemberRepository.findFirstByGroupIdAndRoleAndStatus(id, GroupMemberRole.LEADER, GroupMemberStatus.ACTIVE)
                .ifPresent(current -> {
                    current.setRole(GroupMemberRole.MEMBER);
                    groupMemberRepository.save(current);
                });

        newLeader.setRole(GroupMemberRole.LEADER);
        groupMemberRepository.save(newLeader);

        group.setLeaderCustomerId(request.getCustomerId());
        return toResponse(groupRepository.save(group));
    }

    @Override
    public GroupResponse close(Long id) {
        Group group = findOrThrow(id);
        if (group.getStatus() == GroupStatus.CLOSED) {
            throw new AppException(HttpStatus.CONFLICT, "Group is already closed");
        }
        group.setStatus(GroupStatus.CLOSED);
        return toResponse(groupRepository.save(group));
    }

    @Override
    public List<GroupMemberResponse> getMembers(Long id) {
        findOrThrow(id);
        return groupMemberRepository.findByGroupIdOrderByJoinedAtAsc(id).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Override
    public GroupMemberResponse addMember(Long id, AddGroupMemberRequest request) {
        Group group = findOrThrow(id);
        if (group.getStatus() == GroupStatus.CLOSED) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot add members to a closed group");
        }
        if (groupMemberRepository.existsByGroupIdAndCustomerIdAndStatus(id, request.getCustomerId(), GroupMemberStatus.ACTIVE)) {
            throw new AppException(HttpStatus.CONFLICT, "Customer is already an active member of this group");
        }
        GroupMember member = GroupMember.builder()
                .group(group)
                .customerId(request.getCustomerId())
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        return toMemberResponse(groupMemberRepository.save(member));
    }

    @Override
    public GroupMemberResponse removeMember(Long id, Long customerId) {
        Group group = findOrThrow(id);
        if (customerId.equals(group.getLeaderCustomerId())) {
            throw new AppException(HttpStatus.CONFLICT, "Assign a new leader before removing the current leader");
        }
        GroupMember member = groupMemberRepository
                .findByGroupIdAndCustomerIdAndStatus(id, customerId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active group member for customer", customerId));
        member.setStatus(GroupMemberStatus.LEFT);
        member.setLeftAt(LocalDateTime.now());
        return toMemberResponse(groupMemberRepository.save(member));
    }

    private Group findOrThrow(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group", id));
    }

    private GroupMemberResponse toMemberResponse(GroupMember member) {
        CustomerResponse customer = customerClient.getById(member.getCustomerId()).getData();
        List<CustomerIdentityResponse> identities = customerClient.getIdentities(member.getCustomerId()).getData();
        boolean verified = identities != null && identities.stream().anyMatch(i -> "ACTIVE".equals(i.getStatus()));
        return GroupMemberResponse.builder()
                .id(member.getId())
                .customerId(member.getCustomerId())
                .customerName(customer != null ? customer.getFirstName() + " " + customer.getLastName() : null)
                .role(member.getRole())
                .status(member.getStatus())
                .kycStatus(verified ? "VERIFIED" : "PENDING")
                .joinedAt(member.getJoinedAt())
                .leftAt(member.getLeftAt())
                .build();
    }

    private GroupResponse toResponse(Group group) {
        String branchName = group.getBranchId() != null
                ? branchClient.getById(group.getBranchId()).getData().getName()
                : null;
        String loanProductName = group.getLoanProductId() != null
                ? loanProductClient.getById(group.getLoanProductId()).getData().getName()
                : null;
        String leaderName = null;
        if (group.getLeaderCustomerId() != null) {
            CustomerResponse leader = customerClient.getById(group.getLeaderCustomerId()).getData();
            leaderName = leader != null ? leader.getFirstName() + " " + leader.getLastName() : null;
        }
        long memberCount = groupMemberRepository.countByGroupIdAndStatus(group.getId(), GroupMemberStatus.ACTIVE);

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .code(group.getCode())
                .loanProductId(group.getLoanProductId())
                .loanProductName(loanProductName)
                .branchId(group.getBranchId())
                .branchName(branchName)
                .leaderCustomerId(group.getLeaderCustomerId())
                .leaderName(leaderName)
                .memberCount((int) memberCount)
                .formationDate(group.getFormationDate())
                .status(group.getStatus())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}
