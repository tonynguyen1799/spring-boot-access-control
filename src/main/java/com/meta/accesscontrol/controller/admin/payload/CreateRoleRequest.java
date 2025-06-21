package com.meta.accesscontrol.controller.admin.payload;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(
    @NotBlank String name
) {}