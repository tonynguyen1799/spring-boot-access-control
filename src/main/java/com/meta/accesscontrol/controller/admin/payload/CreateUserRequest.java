package com.meta.accesscontrol.controller.admin.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        List<String> roleTextIds
) {}