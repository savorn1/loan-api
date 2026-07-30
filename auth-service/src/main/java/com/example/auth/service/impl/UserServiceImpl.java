package com.example.auth.service.impl;

import com.example.auth.dto.CreateUserRequest;
import com.example.auth.dto.PageResponse;
import com.example.auth.dto.UpdateRoleRequest;
import com.example.auth.dto.UpdateStatusRequest;
import com.example.auth.dto.UserFilterRequest;
import com.example.auth.dto.UserResponse;
import com.example.auth.entity.SysUser;
import com.example.auth.entity.UserStatus;
import com.example.auth.exception.AppException;
import com.example.auth.redis.UserSessionStore;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.SysUserRepository;
import com.example.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionStore userSessionStore;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(UserFilterRequest filter) {
        List<Specification<SysUser>> conditions = new ArrayList<>();

        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("username")), "%" + filter.getUsername().toLowerCase() + "%"));
        }
        if (filter.getRole() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("role"), filter.getRole()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getStartDate() != null) {
            conditions.add((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate().atStartOfDay()));
        }
        if (filter.getEndDate() != null) {
            conditions.add((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate().atTime(23, 59, 59)));
        }
        Specification<SysUser> spec = Specification.allOf(conditions);

        Sort sort = "asc".equalsIgnoreCase(filter.getSortOrder())
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        Pageable pageable = PageRequest.of(Math.max(filter.getPage() - 1, 0), filter.getSize(), sort);

        Page<UserResponse> page = userRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.of(page);
    }

    @Override
    public UserResponse getUser(Long id) {
        return toResponse(findUser(id));
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(HttpStatus.CONFLICT, "Username already taken: " + request.getUsername());
        }
        SysUser user = SysUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(request.getStatus())
                .build();
        userRepository.save(user);
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateRole(Long id, UpdateRoleRequest request, String actingUsername) {
        SysUser user = findUser(id);
        // An admin can't demote themselves — would leave the system with no way to undo it.
        if (user.getUsername().equals(actingUsername) && user.getRole() != request.getRole()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot change your own role");
        }
        user.setRole(request.getRole());
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user.getId());
        userSessionStore.delete(user.getUuid().toString());
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long id, UpdateStatusRequest request, String actingUsername) {
        SysUser user = findUser(id);
        if (user.getUsername().equals(actingUsername) && request.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot deactivate your own account");
        }
        user.setStatus(request.getStatus());
        userRepository.save(user);
        if (user.getStatus() == UserStatus.INACTIVE) {
            refreshTokenRepository.revokeAllForUser(user.getId());
            userSessionStore.delete(user.getUuid().toString());
        }
        return toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String actingUsername) {
        SysUser user = findUser(id);
        if (user.getUsername().equals(actingUsername)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }
        refreshTokenRepository.revokeAllForUser(user.getId());
        userSessionStore.delete(user.getUuid().toString());
        userRepository.delete(user);
    }

    @Override
    public List<UserResponse> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public String deleteUsers(List<Long> ids, String actingUsername) {
        List<SysUser> users = userRepository.findAllById(ids);
        if (users.isEmpty()) {
            return "No users found for the provided IDs.";
        }
        if (users.stream().anyMatch(u -> u.getUsername().equals(actingUsername))) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }
        users.forEach(u -> {
            refreshTokenRepository.revokeAllForUser(u.getId());
            userSessionStore.delete(u.getUuid().toString());
        });
        userRepository.deleteAll(users);
        return "Deleted " + users.size() + " user(s) successfully.";
    }

    @Override
    @Transactional
    public void forceLogout(Long id) {
        SysUser user = findUser(id);
        // Only revokes refresh tokens and the cached permission session; any access
        // token the user already holds stays valid until it expires naturally.
        refreshTokenRepository.revokeAllForUser(user.getId());
        userSessionStore.delete(user.getUuid().toString());
    }

    @Override
    @Transactional
    public void forceLogoutAll() {
        refreshTokenRepository.revokeAll();
        userSessionStore.deleteAll();
    }

    private SysUser findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }

    private UserResponse toResponse(SysUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .uuid(user.getUuid().toString())
                .username(user.getUsername())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
