package com.meta.accesscontrol.controller.admin.payload;

import com.meta.accesscontrol.model.Privilege;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank String name,
        @Size(max = 255) String description,
        Set<Privilege> privileges
) {}