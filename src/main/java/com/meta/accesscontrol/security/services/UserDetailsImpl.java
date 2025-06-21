package com.meta.accesscontrol.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meta.accesscontrol.model.Role;
import com.meta.accesscontrol.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String textId;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled;

    public UserDetailsImpl(Long id, String textId, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        this.id = id;
        this.textId = textId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public static UserDetailsImpl build(User user) {
        // Collect all distinct privileges from the user's roles
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getPrivileges)
                .flatMap(Set::stream)
                .map(privilege -> new SimpleGrantedAuthority(privilege.name()))
                .distinct()
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getTextId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.isEnabled());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}