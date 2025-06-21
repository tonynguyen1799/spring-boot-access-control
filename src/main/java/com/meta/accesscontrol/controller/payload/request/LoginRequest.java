package com.meta.accesscontrol.controller.payload.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password,
    boolean rememberMe
) {}