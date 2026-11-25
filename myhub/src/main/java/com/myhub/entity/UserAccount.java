package com.myhub.entity;

public record UserAccount(Long id, String username, String password, String nickname, String avatar, boolean enabled)
        implements org.springframework.security.core.userdetails.UserDetails {
    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() { return java.util.List.of(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
