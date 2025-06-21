package com.meta.accesscontrol.controller.payload.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank String currentPassword,
    @NotBlank String newPassword,
    @NotBlank String confirmPassword
) {}