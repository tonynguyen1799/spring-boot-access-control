package com.meta.accesscontrol.controller.admin.payload;

import java.util.Set;
import com.meta.accesscontrol.model.Privilege;
import com.meta.accesscontrol.model.Role;

public record RoleResponse(
        String textId,
        String name,
        String description, // Reverted
        Set<Privilege> privileges
) {
    public static RoleResponse fromRole(Role role) {
        return new RoleResponse(role.getTextId(), role.getName(), role.getDescription(), role.getPrivileges());
    }
}