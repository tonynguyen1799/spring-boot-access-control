package com.meta.accesscontrol.controller.admin.payload;

import java.util.List;

public record UpdateUserRolesRequest(
    List<String> roleTextIds
) {}