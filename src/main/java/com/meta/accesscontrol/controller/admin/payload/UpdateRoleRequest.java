package com.meta.accesscontrol.controller.admin.payload;

import com.meta.accesscontrol.model.Privilege;
import java.util.Set;

public record UpdateRoleRequest(
    String name,
    Set<Privilege> privileges
) {}