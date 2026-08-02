package com.example.loan.service;

import com.example.loan.common.PageResponse;
import com.example.loan.dto.AddGroupMemberRequest;
import com.example.loan.dto.GroupFilterRequest;
import com.example.loan.dto.GroupMemberResponse;
import com.example.loan.dto.GroupRequest;
import com.example.loan.dto.GroupResponse;
import com.example.loan.dto.GroupStatusUpdateRequest;
import com.example.loan.dto.SetGroupLeaderRequest;

import java.util.List;

public interface GroupService {

    GroupResponse create(GroupRequest request);

    GroupResponse getById(Long id);

    PageResponse<GroupResponse> list(GroupFilterRequest filter);

    GroupResponse update(Long id, GroupRequest request);

    GroupResponse updateStatus(Long id, GroupStatusUpdateRequest request);

    GroupResponse setLeader(Long id, SetGroupLeaderRequest request);

    GroupResponse close(Long id);

    List<GroupMemberResponse> getMembers(Long id);

    GroupMemberResponse addMember(Long id, AddGroupMemberRequest request);

    GroupMemberResponse removeMember(Long id, Long customerId);
}
