package com.meta.accesscontrol.controller.payload.response;

import com.meta.accesscontrol.model.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record UserResponse(
        String textId,
        String username,
        String email,
        List<String> authorities,
        UserProfileResponse profile
) {
    public static UserResponse fromUser(User user, Collection<? extends GrantedAuthority> authorities) {
        List<String> authorityStrings = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new UserResponse(
                user.getTextId(),
                user.getUsername(),
                user.getEmail(),
                authorityStrings,
                UserProfileResponse.fromUserProfile(user.getUserProfile())
        );
    }
}
