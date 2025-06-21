package com.meta.accesscontrol.controller.payload.response;

import com.meta.accesscontrol.model.User;
import java.util.List;

public record UserResponse(
    String textId,
    String username,
    String email,
    List<String> authorities
) {
    public static UserResponse fromUser(User user, List<String> authorities) {
        return new UserResponse(
                user.getTextId(),
                user.getUsername(),
                user.getEmail(),
                authorities
        );
    }
}