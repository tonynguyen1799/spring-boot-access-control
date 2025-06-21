package com.meta.accesscontrol.service;

import com.meta.accesscontrol.controller.payload.request.ChangePasswordRequest;
import com.meta.accesscontrol.controller.payload.request.LoginRequest;
import com.meta.accesscontrol.controller.payload.response.UserResponse;
import com.meta.accesscontrol.exception.ResourceNotFoundException;
import com.meta.accesscontrol.model.User;
import com.meta.accesscontrol.repository.UserRepository;
import com.meta.accesscontrol.security.jwt.JwtUtils;
import com.meta.accesscontrol.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public Map<String, String> authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        long accessTokenExpiry = loginRequest.rememberMe() ?
                jwtUtils.getJwtProperties().getLongExpirationMs() :
                jwtUtils.getJwtProperties().getExpirationMs();

        long refreshTokenExpiry = loginRequest.rememberMe() ?
                jwtUtils.getJwtProperties().getLongRefreshExpirationMs() :
                jwtUtils.getJwtProperties().getRefreshExpirationMs();

        String accessToken = jwtUtils.generateJwtToken(authentication, accessTokenExpiry);
        String refreshToken = jwtUtils.generateRefreshToken(userPrincipal, refreshTokenExpiry);

        return Map.of(
                "token", accessToken,
                "refreshToken", refreshToken,
                "type", "Bearer"
        );
    }

    public UserResponse getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = getCurrentAuthenticatedUser();
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return UserResponse.fromUser(user, authorities);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentAuthenticatedUser();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public String refreshToken(String refreshToken) {
        if (refreshToken == null || !jwtUtils.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String username = jwtUtils.getUsernameFromRefreshToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        return jwtUtils.generateJwtToken(authentication);
    }

    private User getCurrentAuthenticatedUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userDetails.getUsername()));
    }
}
