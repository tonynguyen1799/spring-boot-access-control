package com.meta.accesscontrol.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines the fixed set of privileges in the application.
 */
@Getter
@RequiredArgsConstructor
public enum Privilege {
    ROLE_MANAGEMENT_READ("Allows viewing roles and permissions"),
    ROLE_MANAGEMENT_WRITE("Allows creating, updating, and deleting roles"),
    USER_MANAGEMENT_READ("Allows viewing users"),
    USER_MANAGEMENT_WRITE("Allows creating users and managing their roles and status");

    private final String description;
}