package com.meta.accesscontrol.service;

import com.meta.accesscontrol.controller.admin.payload.CreateUserRequest;
import com.meta.accesscontrol.controller.admin.payload.UpdateUserRequest;
import com.meta.accesscontrol.controller.admin.payload.UserFilterRequest;
import com.meta.accesscontrol.controller.payload.response.PaginationResponse;
import com.meta.accesscontrol.controller.payload.response.UserResponse;
import com.meta.accesscontrol.exception.DuplicateResourceException;
import com.meta.accesscontrol.exception.ResourceNotFoundException;
import com.meta.accesscontrol.model.Role;
import com.meta.accesscontrol.model.User;
import com.meta.accesscontrol.repository.RoleRepository;
import com.meta.accesscontrol.repository.UserRepository;
import com.meta.accesscontrol.repository.specs.UserSpecification;
import com.meta.accesscontrol.security.services.UserDetailsImpl;
import com.meta.accesscontrol.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.default-password}")
    private String defaultPassword;

    @Transactional(readOnly = true)
    public PaginationResponse<UserResponse> getUsers(
            int page,
            int size,
            String[] sort,
            UserFilterRequest filterRequest) {

        List<Sort.Order> orders = PaginationUtils.buildSortOrders(
                sort,
                Sort.Order.desc("createdAt")
        );
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));

        Page<User> userPage = userRepository.findAll(new UserSpecification(filterRequest), pageable);

        return new PaginationResponse<>(userPage.map(UserResponse::fromUser));
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(String textId) {
        User user = findUserByTextId(textId);
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        return UserResponse.fromUser(user, userDetails.getAuthorities());
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already in use!");
        }

        User user = new User(request.username(), request.email(), passwordEncoder.encode(defaultPassword));
        assignRolesToUser(user, request.roleTextIds());

        User savedUser = userRepository.save(user);
        return UserResponse.fromUser(savedUser);
    }

    @Transactional
    public UserResponse updateUser(String textId, UpdateUserRequest request) {
        checkSelfModification(textId, "You cannot modify your own account details.");
        User user = findUserByTextId(textId);

        if (request.roleTextIds() != null) {
            assignRolesToUser(user, request.roleTextIds());
        }

        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }

        User updatedUser = userRepository.save(user);
        return UserResponse.fromUser(updatedUser);
    }

    @Transactional
    public void deleteUser(String textId) {
        checkSelfModification(textId, "You cannot delete your own account.");
        User user = findUserByTextId(textId);
        userRepository.delete(user);
    }

    private User findUserByTextId(String textId) {
        return userRepository.findByTextId(textId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with textId: " + textId));
    }

    private void assignRolesToUser(User user, List<String> roleTextIds) {
        if (Objects.isNull(roleTextIds) || roleTextIds.isEmpty()) {
            user.setRoles(new HashSet<>());
            return;
        }
        Set<Role> roles = roleTextIds.stream()
                .map(this::findRoleByTextId)
                .collect(Collectors.toSet());
        user.setRoles(roles);
    }

    private Role findRoleByTextId(String textId) {
        return roleRepository.findByTextId(textId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with textId: " + textId));
    }

    private void checkSelfModification(String targetUserTextId, String errorMessage) {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getTextId().equals(targetUserTextId)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}