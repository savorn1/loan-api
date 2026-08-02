package com.example.loan.controller;

import com.example.loan.common.ApiResponse;
import com.example.loan.common.PageResponse;
import com.example.loan.dto.AddGroupMemberRequest;
import com.example.loan.dto.GroupFilterRequest;
import com.example.loan.dto.GroupMemberResponse;
import com.example.loan.dto.GroupRequest;
import com.example.loan.dto.GroupResponse;
import com.example.loan.dto.GroupStatusUpdateRequest;
import com.example.loan.dto.SetGroupLeaderRequest;
import com.example.loan.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> create(@Valid @RequestBody GroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Group created", groupService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<GroupResponse>> list(@ModelAttribute GroupFilterRequest filter) {
        return ResponseEntity.ok(groupService.list(filter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> update(
            @PathVariable Long id, @Valid @RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Group updated", groupService.update(id, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<GroupResponse>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody GroupStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Group status updated", groupService.updateStatus(id, request)));
    }

    @PutMapping("/{id}/leader")
    public ResponseEntity<ApiResponse<GroupResponse>> setLeader(
            @PathVariable Long id, @Valid @RequestBody SetGroupLeaderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Group leader updated", groupService.setLeader(id, request)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<GroupResponse>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Group closed", groupService.close(id)));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getMembers(id)));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<GroupMemberResponse>> addMember(
            @PathVariable Long id, @Valid @RequestBody AddGroupMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added", groupService.addMember(id, request)));
    }

    @PutMapping("/{id}/members/{customerId}/leave")
    public ResponseEntity<ApiResponse<GroupMemberResponse>> removeMember(
            @PathVariable Long id, @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success("Member left the group", groupService.removeMember(id, customerId)));
    }
}
