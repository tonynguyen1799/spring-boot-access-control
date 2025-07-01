package com.meta.accesscontrol.service;

import com.meta.accesscontrol.controller.admin.payload.CreateUserRequest;
import com.meta.accesscontrol.controller.admin.payload.UpdateUserRequest;
import com.meta.accesscontrol.controller.admin.payload.UserFilterRequest;
import com.meta.accesscontrol.controller.payload.response.PaginationResponse;
import com.meta.accesscontrol.controller.payload.response.UserResponse;
import com.meta.accesscontrol.exception.DuplicateResourceException;
import com.meta.accesscontrol.exception.ResourceNotFoundException;
import com.meta.accesscontrol.model.Privilege;
import com.meta.accesscontrol.model.Role;
import com.meta.accesscontrol.model.User;
import com.meta.accesscontrol.model.UserProfile;
import com.meta.accesscontrol.repository.RoleRepository;
import com.meta.accesscontrol.repository.UserRepository;
import com.meta.accesscontrol.repository.specs.UserSpecification;
import com.meta.accesscontrol.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Setup main user object
        user = new User("testuser", "test@meta.com", "password");
        user.setTextId("user-text-id");
        user.setCreatedAt(Instant.now());
        user.setCreatedBy("system");

        UserProfile userProfile = new UserProfile(user);
        userProfile.setFirstName("Test");
        user.setUserProfile(userProfile);

        userRole = new Role("ROLE_USER");
        userRole.setTextId("role-text-id");

        // Manually inject the default password property for testing
        ReflectionTestUtils.setField(userService, "defaultPassword", "Password123!");

        // Mock the SecurityContext to simulate a logged-in user
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetUsers_withNoFilters_shouldReturnPagedUserResponse() {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(UserSpecification.class), any(Pageable.class))).thenReturn(userPage);
        UserFilterRequest filterRequest = new UserFilterRequest(null, null, null);

        // Act
        PaginationResponse<UserResponse> result = userService.getUsers(0, 10, null, filterRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.content().size());
        UserResponse summary = result.content().getFirst();
        assertEquals("testuser", summary.username());
        assertNotNull(summary.createdAt());
        assertEquals("system", summary.createdBy());
        assertNull(summary.privileges());
        assertNull(summary.profile());
        verify(userRepository).findAll(any(UserSpecification.class), any(Pageable.class));
    }

    @Test
    void testGetUsers_withSearchFilter_shouldCallSpecification() {
        // Arrange
        Page<User> userPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAll(any(UserSpecification.class), any(Pageable.class))).thenReturn(userPage);
        UserFilterRequest filterRequest = new UserFilterRequest("test", null, null);
        ArgumentCaptor<UserSpecification> specCaptor = ArgumentCaptor.forClass(UserSpecification.class);

        // Act
        userService.getUsers(0, 10, null, filterRequest);

        // Assert
        verify(userRepository).findAll(specCaptor.capture(), any(Pageable.class));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void testGetUser_whenUserExists_shouldReturnDetailedUserResponse() {
        // Arrange
        user.getRoles().add(userRole); // Add role to user for privilege check
        userRole.setPrivileges(Set.of(Privilege.USER_MANAGEMENT_READ));
        when(userRepository.findByTextId("user-text-id")).thenReturn(Optional.of(user));

        // Act
        UserResponse result = userService.getUser("user-text-id");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals("Test", result.profile().firstName());
        assertNotNull(result.privileges());
        assertTrue(result.privileges().contains("USER_MANAGEMENT_READ"));
        assertNotNull(result.createdAt());
        assertEquals("system", result.createdBy());
        verify(userRepository).findByTextId("user-text-id");
    }

    @Test
    void testGetUser_whenUserDoesNotExist_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findByTextId("non-existent-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUser("non-existent-id"));
    }

    @Test
    void testCreateUser_withRoles_shouldCreateUserWithRoles() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("newuser", "new@meta.com", List.of("role-text-id"));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@meta.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByTextId("role-text-id")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setRoles(Set.of(userRole));
            savedUser.setUserProfile(new UserProfile(savedUser));
            return savedUser;
        });

        // Act
        UserResponse result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.username());
        assertTrue(result.roles().contains("ROLE_USER"));
        assertNull(result.privileges()); // Privileges should be null for this response
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_whenUsernameExists_shouldThrowDuplicateResourceException() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("existinguser", "new@meta.com", Collections.emptyList());
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_whenEmailExists_shouldThrowDuplicateResourceException() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("newuser", "existing@meta.com", Collections.emptyList());
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@meta.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser_withRoles_shouldUpdateUserRoles() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest(List.of("role-text-id"), true);
        mockSecurityContext("admin-user", "admin-text-id");
        when(userRepository.findByTextId("user-text-id")).thenReturn(Optional.of(user));
        when(roleRepository.findByTextId("role-text-id")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse result = userService.updateUser("user-text-id", request);

        // Assert
        assertNotNull(result);
        assertTrue(result.enabled());
        assertTrue(result.roles().contains("ROLE_USER"));
        verify(userRepository).save(any(User.class));
        verify(roleRepository).findByTextId("role-text-id");
    }

    @Test
    void testUpdateUser_whenUserNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest(null, true);
        mockSecurityContext("admin-user", "admin-text-id");
        when(userRepository.findByTextId("non-existent-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser("non-existent-id", request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser_whenUpdatingSelf_shouldThrowIllegalArgumentException() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest(null, false);
        mockSecurityContext("testuser", "user-text-id");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser("user-text-id", request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser_whenUserNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        mockSecurityContext("admin-user", "admin-text-id");
        when(userRepository.findByTextId("non-existent-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser("non-existent-id"));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void testDeleteUser_whenUserExists_shouldDeleteUser() {
        // Arrange
        mockSecurityContext("admin-user", "admin-text-id");
        when(userRepository.findByTextId("user-text-id")).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // Act
        userService.deleteUser("user-text-id");

        // Assert
        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteUser_whenDeletingSelf_shouldThrowIllegalArgumentException() {
        // Arrange
        mockSecurityContext("testuser", "user-text-id");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser("user-text-id"));
        verify(userRepository, never()).delete(any(User.class));
    }

    private void mockSecurityContext(String username, String textId) {
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, textId, username, "test@test.com", "password", Collections.emptyList(), true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }
}