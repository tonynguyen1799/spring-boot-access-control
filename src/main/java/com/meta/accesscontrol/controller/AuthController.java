package com.meta.accesscontrol.controller;

import com.meta.accesscontrol.controller.payload.request.ChangePasswordRequest;
import com.meta.accesscontrol.controller.payload.request.LoginRequest;
import com.meta.accesscontrol.controller.payload.request.RefreshTokenRequest;
import com.meta.accesscontrol.controller.payload.request.UpdateUserProfileRequest;
import com.meta.accesscontrol.controller.payload.response.UserResponse;
import com.meta.accesscontrol.controller.payload.response.JsonResponse;
import com.meta.accesscontrol.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public JsonResponse<Map<String, String>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, String> data = authService.authenticateUser(loginRequest);
        return new JsonResponse<>(HttpStatus.OK.value(), "Login successful", data);
    }

    @GetMapping("/me")
    public JsonResponse<UserResponse> getCurrentUser() {
        UserResponse userResponse = authService.getCurrentUser();
        return new JsonResponse<>(HttpStatus.OK.value(), "User details fetched successfully", userResponse);
    }

    @PutMapping("/me")
    public JsonResponse<UserResponse> updateCurrentUserProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        UserResponse updatedUser = authService.updateCurrentUserProfile(request);
        return new JsonResponse<>(HttpStatus.OK.value(), "Profile updated successfully", updatedUser);
    }

    @PostMapping("/change-password")
    public JsonResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        authService.changePassword(changePasswordRequest);
        return new JsonResponse<>(HttpStatus.OK.value(), "Password changed successfully!", null);
    }

    @PostMapping("/refresh")
    public JsonResponse<Map<String, String>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String newAccessToken = authService.refreshToken(request.refreshToken());
        Map<String, String> data = Map.of("token", newAccessToken, "type", "Bearer");
        return new JsonResponse<>(HttpStatus.OK.value(), "Token refreshed", data);
    }
}
