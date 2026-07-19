package com.example.auth.service.impl;

import com.example.auth.dto.CreateUserRequest;
import com.example.auth.dto.PageResponse;
import com.example.auth.dto.UpdateRoleRequest;
import com.example.auth.dto.UpdateStatusRequest;
import com.example.auth.dto.UserFilterRequest;
import com.example.auth.dto.UserResponse;
import com.example.auth.entity.User;
import com.example.auth.exception.AppException;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(UserFilterRequest filter) {
        List<Specification<User>> conditions = new ArrayList<>();

        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("username")), "%" + filter.getUsername().toLowerCase() + "%"));
        }
        if (filter.getRole() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("role"), filter.getRole()));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        if (filter.getStartDate() != null) {
            conditions.add((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate().atStartOfDay()));
        }
        if (filter.getEndDate() != null) {
            conditions.add((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate().atTime(23, 59, 59)));
        }
        Specification<User> spec = Specification.allOf(conditions);

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
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(request.getActive())
                .build();
        userRepository.save(user);
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateRole(Long id, UpdateRoleRequest request, String actingUsername) {
        User user = findUser(id);
        // An admin can't demote themselves — would leave the system with no way to undo it.
        if (user.getUsername().equals(actingUsername) && user.getRole() != request.getRole()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot change your own role");
        }
        user.setRole(request.getRole());
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user.getId());
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long id, UpdateStatusRequest request, String actingUsername) {
        User user = findUser(id);
        if (user.getUsername().equals(actingUsername) && !request.getActive()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot deactivate your own account");
        }
        user.setActive(request.getActive());
        userRepository.save(user);
        if (!user.isActive()) {
            refreshTokenRepository.revokeAllForUser(user.getId());
        }
        return toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String actingUsername) {
        User user = findUser(id);
        if (user.getUsername().equals(actingUsername)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }
        refreshTokenRepository.revokeAllForUser(user.getId());
        userRepository.delete(user);
    }

    @Override
    public List<UserResponse> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public String deleteUsers(List<Long> ids, String actingUsername) {
        List<User> users = userRepository.findAllById(ids);
        if (users.isEmpty()) {
            return "No users found for the provided IDs.";
        }
        if (users.stream().anyMatch(u -> u.getUsername().equals(actingUsername))) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }
        users.forEach(u -> refreshTokenRepository.revokeAllForUser(u.getId()));
        userRepository.deleteAll(users);
        return "Deleted " + users.size() + " user(s) successfully.";
    }

    @Override
    @Transactional
    public void forceLogout(Long id) {
        User user = findUser(id);
        // Only revokes refresh tokens; any access token the user already holds stays valid until it expires naturally.
        refreshTokenRepository.revokeAllForUser(user.getId());
    }

    @Override
    @Transactional
    public void forceLogoutAll() {
        refreshTokenRepository.revokeAll();
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
