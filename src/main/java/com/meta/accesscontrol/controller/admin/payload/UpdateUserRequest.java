package com.meta.accesscontrol.controller.admin.payload;

import java.util.List;

public record UpdateUserRequest(
        List<String> roleTextIds,
        Boolean enabled
) {}