package com.meta.accesscontrol.controller.admin.payload;

import com.meta.accesscontrol.model.Privilege;

public record PrivilegeResponse(
    String name,
    String description
) {
    public static PrivilegeResponse fromPrivilege(Privilege privilege) {
        return new PrivilegeResponse(privilege.name(), privilege.getDescription());
    }
}