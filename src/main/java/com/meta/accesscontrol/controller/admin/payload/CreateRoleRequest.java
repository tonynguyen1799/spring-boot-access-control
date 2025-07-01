package com.meta.accesscontrol.controller.admin.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank String name,
        @Size(max = 255) String description // Reverted
) {}