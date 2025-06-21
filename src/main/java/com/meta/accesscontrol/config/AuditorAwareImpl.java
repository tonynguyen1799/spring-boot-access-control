package com.meta.accesscontrol.config;

import com.meta.accesscontrol.security.services.UserDetailsImpl;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("system");
        }
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Optional.of(userPrincipal.getUsername());
    }
}