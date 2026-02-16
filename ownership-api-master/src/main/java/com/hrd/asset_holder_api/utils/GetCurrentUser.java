package com.hrd.asset_holder_api.utils;

import com.hrd.asset_holder_api.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for retrieving current authenticated user information.
 */
@Slf4j
public class GetCurrentUser {
    /**
     * Get current authenticated user ID.
     *
     * @return The user ID of the currently authenticated user
     */
    public static Integer currentId(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.debug("Retrieved current user ID: {}", user.getUserId());
        return user.getUserId();
    }
}
