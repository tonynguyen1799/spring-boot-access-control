package com.meta.accesscontrol.controller.admin.payload;

import com.meta.accesscontrol.model.Role;
import com.meta.accesscontrol.model.User;

import java.util.List;
import java.util.stream.Collectors;

public record AdminUserSummaryResponse(
        String textId,
        String username,
        String email,
        boolean enabled,
        List<String> roles
) {
    public static AdminUserSummaryResponse fromUser(User user) {
        return new AdminUserSummaryResponse(
                user.getTextId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList())
        );
    }
}
