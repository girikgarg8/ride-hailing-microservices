package com.girikgarg.uberauthservice.helpers;

import com.girikgarg.uberentityservice.models.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Why we need this class?
 * Because Spring Security works on UserDetails polymorphic type for auth
 * This adapter wraps our Driver/Passenger entities to make them compatible with Spring Security
 */
public class AuthUserDetails implements UserDetails {
    private Long id;
    private String username; // email
    private String password;
    private Role role;

    public AuthUserDetails(Long id, String email, String password, Role role) {
        this.id = id;
        this.username = email;
        this.password = password;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert role to Spring Security authority
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    
    public Role getRole() {
        return this.role;
    }
    
    public Long getId() {
        return this.id;
    }
}
