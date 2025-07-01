package com.meta.accesscontrol.controller.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.meta.accesscontrol.model.Role;
import com.meta.accesscontrol.model.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        String textId,
        String username,
        String email,
        boolean enabled,
        List<String> roles,
        List<String> privileges,
        UserProfileResponse profile,
        Long createdAt,
        String createdBy,
        Long updatedAt,
        String updatedBy
) {
    /**
     * Creates a full UserResponse with privileges, typically for a detailed view.
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
                authorityStrings,
                UserProfileResponse.fromUserProfile(user.getUserProfile()),
                user.getCreatedAt() != null ? user.getCreatedAt().toEpochMilli() : null,
                user.getCreatedBy(),
                user.getUpdatedAt() != null ? user.getUpdatedAt().toEpochMilli() : null,
                user.getUpdatedBy()
        );
    }

    /**
     * Creates a summary UserResponse without privileges and profile, suitable for lists.
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
                null,
                null,
                user.getCreatedAt() != null ? user.getCreatedAt().toEpochMilli() : null,
                user.getCreatedBy(),
                user.getUpdatedAt() != null ? user.getUpdatedAt().toEpochMilli() : null,
                user.getUpdatedBy()
        );
    }
}