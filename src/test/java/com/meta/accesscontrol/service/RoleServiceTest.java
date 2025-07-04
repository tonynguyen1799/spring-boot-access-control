package com.meta.accesscontrol.service;

import com.meta.accesscontrol.controller.admin.payload.CreateRoleRequest;
import com.meta.accesscontrol.controller.admin.payload.UpdateRoleRequest;
import com.meta.accesscontrol.exception.DuplicateResourceException;
import com.meta.accesscontrol.exception.ResourceNotFoundException;
import com.meta.accesscontrol.model.Privilege;
import com.meta.accesscontrol.model.Role;
import com.meta.accesscontrol.repository.RoleRepository;
import com.meta.accesscontrol.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;

    private Role adminRole;
    private Role supportRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role("ROLE_ADMIN");
        adminRole.setTextId("admin-role-id");
        adminRole.setPrivileges(Set.of(Privilege.ROLE_MANAGEMENT_READ, Privilege.ROLE_MANAGEMENT_WRITE));

        supportRole = new Role("ROLE_SUPPORT");
        supportRole.setTextId("support-role-id");
    }

    @Test
    void testCreateRole_shouldSucceed() {
        CreateRoleRequest request = new CreateRoleRequest("ROLE_NEW", "New Role", Collections.emptySet());
        when(roleRepository.findByName("ROLE_NEW")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = roleService.createRole(request);

        assertNotNull(result);
        assertEquals("ROLE_NEW", result.name());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void testCreateRole_whenNameExists_shouldThrowDuplicateResourceException() {
        CreateRoleRequest request = new CreateRoleRequest("ROLE_ADMIN", "Admin Role", null);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        assertThrows(DuplicateResourceException.class, () -> roleService.createRole(request));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testUpdateRole_whenNotProtected_shouldSucceed() {
        UpdateRoleRequest request = new UpdateRoleRequest("ROLE_SUPPORT_UPDATED", "Updated Desc", Set.of(Privilege.USER_MANAGEMENT_READ));
        when(roleRepository.findByTextId("support-role-id")).thenReturn(Optional.of(supportRole));
        when(roleRepository.save(any(Role.class))).thenReturn(supportRole);

        var result = roleService.updateRole("support-role-id", request);

        assertNotNull(result);
        assertEquals("ROLE_SUPPORT_UPDATED", result.name());
        assertEquals("Updated Desc", result.description());
        assertTrue(result.privileges().contains(Privilege.USER_MANAGEMENT_READ));
        verify(roleRepository).save(supportRole);
    }

    @Test
    void testUpdateRole_whenProtected_shouldThrowIllegalStateException() {
        UpdateRoleRequest request = new UpdateRoleRequest("NEW_NAME", null, null);
        when(roleRepository.findByTextId("admin-role-id")).thenReturn(Optional.of(adminRole));

        var exception = assertThrows(IllegalStateException.class, () -> roleService.updateRole("admin-role-id", request));
        assertEquals("Cannot modify a protected role: ROLE_ADMIN", exception.getMessage());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testDeleteRole_whenNotInUseAndNotProtected_shouldSucceed() {
        when(roleRepository.findByTextId("support-role-id")).thenReturn(Optional.of(supportRole));
        when(userRepository.existsByRoles_TextId("support-role-id")).thenReturn(false);

        roleService.deleteRole("support-role-id");

        verify(roleRepository).delete(supportRole);
    }

    @Test
    void testDeleteRole_whenRoleIsProtected_shouldThrowIllegalStateException() {
        when(roleRepository.findByTextId("admin-role-id")).thenReturn(Optional.of(adminRole));

        var exception = assertThrows(IllegalStateException.class, () -> roleService.deleteRole("admin-role-id"));
        assertEquals("Cannot delete a protected role: ROLE_ADMIN", exception.getMessage());
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void testDeleteRole_whenRoleInUse_shouldThrowIllegalStateException() {
        when(roleRepository.findByTextId("support-role-id")).thenReturn(Optional.of(supportRole));
        when(userRepository.existsByRoles_TextId("support-role-id")).thenReturn(true);

        var exception = assertThrows(IllegalStateException.class, () -> roleService.deleteRole("support-role-id"));
        assertEquals("Cannot delete role: one or more users are currently assigned to this role.", exception.getMessage());
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void testGetRole_whenNotFound_shouldThrowResourceNotFoundException() {
        when(roleRepository.findByTextId("not-found-id")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roleService.getRole("not-found-id"));
    }
}