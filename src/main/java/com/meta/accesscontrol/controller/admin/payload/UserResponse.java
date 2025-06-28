package com.meta.accesscontrol.controller.admin.payload;

import java.util.List;
import com.meta.accesscontrol.model.User;
import com.meta.accesscontrol.model.Role;
import com.meta.accesscontrol.model.Privilege;
import java.util.stream.Collectors;

public record UserResponse(
    String textId,
    String username,
    String email,
    boolean enabled,
    List<String> roles,
    List<String> privileges
) {
    public static UserResponse fromUser(User user) {
        return new UserResponse(
                user.getTextId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList()),
                user.getRoles().stream().flatMap(role -> role.getPrivileges().stream()).map(Privilege::name).collect(Collectors.toList())
        );
    }
}