package com.hrd.asset_holder_api.utils;

import com.hrd.asset_holder_api.model.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class GetCurrentUser {
    //Get current user id
    public static Integer currentId(){
        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(user);
        return user.getUserId();
    }
}
