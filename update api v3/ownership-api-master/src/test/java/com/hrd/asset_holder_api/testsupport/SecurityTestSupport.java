package com.hrd.asset_holder_api.testsupport;

import com.hrd.asset_holder_api.jwt.JwtUtil;
import com.hrd.asset_holder_api.service.AppUserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Small helper to make controller security tests predictable.
 *
 * We mock JwtUtil + AppUserService so JwtAuthFilter authenticates requests that contain
 * Authorization: Bearer <token>.
 */
public final class SecurityTestSupport {
    private SecurityTestSupport() {}

    public static final String GOOD_TOKEN = "test-token";

    public static UserDetails adminUserDetails() {
        return new User(
                "admin",
                "N/A",
                List.of(new SimpleGrantedAuthority("ADMIN"))
        );
    }

    public static UserDetails normalUserDetails() {
        // Any non-ADMIN authority is enough for endpoints that only require authentication.
        return new User(
                "user",
                "N/A",
                List.of(new SimpleGrantedAuthority("USER"))
        );
    }

    public static void mockJwtAsAdmin(JwtUtil jwtUtil, AppUserService appUserService) {
        when(jwtUtil.extractUsername(anyString())).thenReturn("admin");
        when(appUserService.loadUserByUsername("admin")).thenReturn(adminUserDetails());
        when(jwtUtil.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
    }

    public static void mockJwtAsUser(JwtUtil jwtUtil, AppUserService appUserService) {
        when(jwtUtil.extractUsername(anyString())).thenReturn("user");
        when(appUserService.loadUserByUsername("user")).thenReturn(normalUserDetails());
        when(jwtUtil.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
    }
}
