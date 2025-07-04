package com.meta.accesscontrol.service;

import com.meta.accesscontrol.controller.admin.payload.CreateRoleRequest;
import com.meta.accesscontrol.controller.admin.payload.RoleResponse;
import com.meta.accesscontrol.controller.admin.payload.UpdateRoleRequest;
import com.meta.accesscontrol.exception.DuplicateResourceException;
import com.meta.accesscontrol.exception.ResourceNotFoundException;
import com.meta.accesscontrol.model.Role;
import com.meta.accesscontrol.repository.RoleRepository;
import com.meta.accesscontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private static final Set<String> PROTECTED_ROLES = Set.of("ROLE_ADMIN", "ROLE_USER");

    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::fromRole)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponse getRole(String textId) {
        Role role = findRoleByTextId(textId);
        return RoleResponse.fromRole(role);
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        validateRoleName(request.name(), null);

        Role newRole = new Role(request.name());
        newRole.setDescription(request.description());
        newRole.setPrivileges(request.privileges());
        Role savedRole = roleRepository.save(newRole);
        return RoleResponse.fromRole(savedRole);
    }

    @Transactional
    public RoleResponse updateRole(String textId, UpdateRoleRequest request) {
        Role role = findRoleByTextId(textId);
        if (isRoleProtected(role)) {
            throw new IllegalStateException("Cannot modify a protected role: " + role.getName());
        }

        if (StringUtils.hasText(request.name())) {
            validateRoleName(request.name(), role);
            role.setName(request.name());
        }

        if (request.description() != null) {
            role.setDescription(request.description());
        }

        if (Objects.nonNull(request.privileges())) {
            role.setPrivileges(request.privileges());
        }

        Role updatedRole = roleRepository.save(role);
        return RoleResponse.fromRole(updatedRole);
    }

    @Transactional
    public void deleteRole(String textId) {
        Role role = findRoleByTextId(textId);

        if (isRoleProtected(role)) {
            throw new IllegalStateException("Cannot delete a protected role: " + role.getName());
        }

        if (userRepository.existsByRoles_TextId(role.getTextId())) {
            throw new IllegalStateException("Cannot delete role: one or more users are currently assigned to this role.");
        }

        roleRepository.delete(role);
    }

    private Role findRoleByTextId(String textId) {
        return roleRepository.findByTextId(textId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with textId: " + textId));
    }

    private void validateRoleName(String name, Role existingRole) {
        roleRepository.findByName(name)
                .ifPresent(role -> {
                    if (Objects.nonNull(existingRole) || !role.getId().equals(existingRole.getId())) {
                        throw new DuplicateResourceException("Role with name '" + name + "' already exists.");
                    }
                });
    }

    private boolean isRoleProtected(Role role) {
        return PROTECTED_ROLES.contains(role.getName());
    }
}