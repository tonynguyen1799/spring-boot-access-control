package com.meta.accesscontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.accesscontrol.config.JpaAuditingConfiguration;
import com.meta.accesscontrol.config.JwtProperties;
import com.meta.accesscontrol.controller.payload.request.ChangePasswordRequest;
import com.meta.accesscontrol.controller.payload.request.LoginRequest;
import com.meta.accesscontrol.controller.payload.request.RefreshTokenRequest;
import com.meta.accesscontrol.controller.payload.request.UpdateUserProfileRequest;
import com.meta.accesscontrol.controller.payload.response.UserResponse;
import com.meta.accesscontrol.security.UnauthorizedHandler;
import com.meta.accesscontrol.security.jwt.AuthEntryPointJwt;
import com.meta.accesscontrol.security.jwt.JwtUtils;
import com.meta.accesscontrol.security.services.UserDetailsServiceImpl;
import com.meta.accesscontrol.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        // --- THIS IS THE COMPLETE FIX ---
        // 1. Exclude all data source and JPA auto-configurations.
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class
        },
        // 2. Also, specifically filter out your custom JpaAuditingConfiguration.
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JpaAuditingConfiguration.class
        )
)
class AuthControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean public AuthService authService() { return Mockito.mock(AuthService.class); }
        @Bean public UserDetailsServiceImpl userDetailsService() { return Mockito.mock(UserDetailsServiceImpl.class); }
        @Bean public AuthEntryPointJwt authEntryPointJwt() { return Mockito.mock(AuthEntryPointJwt.class); }
        @Bean public UnauthorizedHandler unauthorizedHandler() { return Mockito.mock(UnauthorizedHandler.class); }
        @Bean public JwtUtils jwtUtils() { return Mockito.mock(JwtUtils.class); }
        @Bean public JwtProperties jwtProperties() { return Mockito.mock(JwtProperties.class); }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse("user-text-id", "testuser", "test@meta.com", true, null, null, null, null, null, null, null);
    }

    @Test
    void testAuthenticateUser_shouldReturnOk() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password", false);
        Map<String, String> tokenMap = Map.of("token", "test-token");
        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(tokenMap);

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("test-token"));
    }

    @Test
    @WithMockUser
    void testGetCurrentUser_shouldReturnOk() throws Exception {
        when(authService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User details fetched successfully"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser
    void testUpdateCurrentUserProfile_shouldReturnOk() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest("New", "Name", null, null, "12345", null, null, null, null);
        when(authService.updateCurrentUserProfile(any(UpdateUserProfileRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
    }

    @Test
    @WithMockUser
    void testChangePassword_shouldReturnOk() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("old", "new", "new");
        doNothing().when(authService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Password changed successfully!"));
    }

    @Test
    void testRefreshToken_shouldReturnOk() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        when(authService.refreshToken(anyString())).thenReturn("new-access-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Token refreshed"))
                .andExpect(jsonPath("$.data.token").value("new-access-token"));
    }
}