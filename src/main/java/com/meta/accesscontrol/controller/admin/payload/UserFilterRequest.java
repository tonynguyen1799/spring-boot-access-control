package com.meta.accesscontrol.controller.admin.payload;

import java.util.List;

public record UserFilterRequest(
    String username,
    String email,
    List<String> roleTextIds,
    Boolean enabled
) {}