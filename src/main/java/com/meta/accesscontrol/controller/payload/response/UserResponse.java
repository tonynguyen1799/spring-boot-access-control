package com.meta.accesscontrol.controller.payload.response;

import com.meta.accesscontrol.model.Role;
import com.meta.accesscontrol.model.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record UserResponse(
        String textId,
        String username,
        String email,
        boolean enabled,
        List<String> roles,
        List<String> privileges,
        UserProfileResponse profile
) {
    /**
     * Creates a UserResponse from a User entity, typically for admin-facing endpoints.
     */
    public static UserResponse fromUser(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new UserResponse(
                user.getTextId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                roleNames,
                null, // Privileges are not included by default here
                UserProfileResponse.fromUserProfile(user.getUserProfile())
        );
    }

    /**
     * Creates a UserResponse from a User entity and their authorities,
     * typically after a successful authentication.
     */
    public static UserResponse fromUser(User user, Collection<? extends GrantedAuthority> authorities) {
        List<String> authorityStrings = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new UserResponse(
                user.getTextId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                roleNames,
                authorityStrings, // This list is now mapped to the 'privileges' field
                UserProfileResponse.fromUserProfile(user.getUserProfile())
        );
    }
}