package com.hrd.asset_holder_api.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails{
    private Integer userId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fullName;
    private String username;

    private String password;

    @NotNull(message = "Department cannot be null")
    @Min(value = 1, message = "Department must be a positive number")
    private Integer department;

    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email format")
    @Email(message = "Email should be valid")
    @Schema(example = "john.doe@example.com", description = "Email address of the user")
    @Size(max = 50, message = "Email must be less than or equal to 255 characters")
    private String email;

    @JsonIgnore
    private String roles;


    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(roles));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
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
}
