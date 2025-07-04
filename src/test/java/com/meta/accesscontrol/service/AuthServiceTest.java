package com.meta.accesscontrol.service;

import com.meta.accesscontrol.config.JwtProperties;
import com.meta.accesscontrol.controller.payload.request.ChangePasswordRequest;
import com.meta.accesscontrol.controller.payload.request.LoginRequest;
import com.meta.accesscontrol.controller.payload.request.UpdateUserProfileRequest;
import com.meta.accesscontrol.controller.payload.response.UserResponse;
import com.meta.accesscontrol.exception.ResourceNotFoundException;
import com.meta.accesscontrol.model.User;
import com.meta.accesscontrol.model.UserProfile;
import com.meta.accesscontrol.repository.UserRepository;
import com.meta.accesscontrol.security.jwt.JwtUtils;
import com.meta.accesscontrol.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User user;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@meta.com", "encodedPassword");
        user.setTextId("user-text-id");
        user.setUserProfile(new UserProfile(user));

        userDetails = new UserDetailsImpl(1L, "user-text-id", "testuser", "test@meta.com", "encodedPassword", Collections.emptyList(), true);

        // Mock the SecurityContext to simulate a logged-in user for relevant tests
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    }

    @Test
    void testAuthenticateUser_shouldReturnTokens() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password", false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.getJwtProperties()).thenReturn(jwtProperties);
        when(jwtProperties.getExpirationMs()).thenReturn(3600000L);
        when(jwtProperties.getRefreshExpirationMs()).thenReturn(7200000L);
        when(jwtUtils.generateJwtToken(any(Authentication.class), anyLong())).thenReturn("accessToken");
        when(jwtUtils.generateRefreshToken(any(UserDetailsImpl.class), anyLong())).thenReturn("refreshToken");

        // Act
        Map<String, String> tokens = authService.authenticateUser(loginRequest);

        // Assert
        assertNotNull(tokens);
        assertEquals("accessToken", tokens.get("token"));
        assertEquals("refreshToken", tokens.get("refreshToken"));
        assertEquals("Bearer", tokens.get("type"));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testGetCurrentUser_shouldReturnUserResponse() {
        // Arrange
        mockSecurityContext();

        // Act
        UserResponse result = authService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals("user-text-id", result.textId());
    }

    @Test
    void testUpdateCurrentUserProfile_shouldUpdateAndReturnUser() {
        // Arrange
        mockSecurityContext();
        UpdateUserProfileRequest request = new UpdateUserProfileRequest("New", "Name", null, null, "12345", null, null, null, null);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponse result = authService.updateCurrentUserProfile(request);

        // Assert
        assertNotNull(result);
        assertEquals("New", user.getUserProfile().getFirstName());
        assertEquals("Name", user.getUserProfile().getLastName());
        assertEquals("12345", user.getUserProfile().getPhoneNumber());
        verify(userRepository).save(user);
    }

    @Test
    void testChangePassword_withCorrectCurrentPassword_shouldChangePassword() {
        // Arrange
        mockSecurityContext();
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword", "newPassword");
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        // Act
        authService.changePassword(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("newEncodedPassword", userCaptor.getValue().getPassword());
    }

    @Test
    void testChangePassword_withIncorrectCurrentPassword_shouldThrowException() {
        // Arrange
        mockSecurityContext();
        ChangePasswordRequest request = new ChangePasswordRequest("wrongOldPassword", "newPassword", "newPassword");
        when(passwordEncoder.matches("wrongOldPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.changePassword(request));
        assertEquals("Incorrect current password", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testChangePassword_whenNewPasswordsDoNotMatch_shouldThrowException() {
        // Arrange
        mockSecurityContext();
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword", "differentNewPassword");
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.changePassword(request));
        assertEquals("New passwords do not match", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRefreshToken_withValidToken_shouldReturnNewAccessToken() {
        // Arrange
        String refreshToken = "validRefreshToken";
        when(jwtUtils.validateRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromRefreshToken(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("newAccessToken");

        // Act
        String newAccessToken = authService.refreshToken(refreshToken);

        // Assert
        assertEquals("newAccessToken", newAccessToken);
    }

    @Test
    void testRefreshToken_withInvalidToken_shouldThrowException() {
        // Arrange
        String refreshToken = "invalidRefreshToken";
        when(jwtUtils.validateRefreshToken(refreshToken)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.refreshToken(refreshToken));
        assertEquals("Invalid or expired refresh token", exception.getMessage());
    }

    @Test
    void testRefreshToken_whenUserNotFound_shouldThrowException() {
        // Arrange
        String refreshToken = "validRefreshToken";
        when(jwtUtils.validateRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromRefreshToken(refreshToken)).thenReturn("nonexistentuser");
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.refreshToken(refreshToken));
    }
}