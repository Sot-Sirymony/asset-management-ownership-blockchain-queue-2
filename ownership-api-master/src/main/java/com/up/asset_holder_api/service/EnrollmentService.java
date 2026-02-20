package com.up.asset_holder_api.service;

import com.up.asset_holder_api.model.entity.User;
import com.up.asset_holder_api.model.request.UserPassword;
import com.up.asset_holder_api.model.request.UserRegister;
import com.up.asset_holder_api.model.request.UserRequest;
import com.up.asset_holder_api.model.response.UserRequestResponse;
import com.up.asset_holder_api.model.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface EnrollmentService {
    UserRegister registerUser(UserRegister user);

    List<UserResponse> getAllUser(Integer size, Integer page);

    UserRequest updateUser(Integer id,@Valid UserRequest userRequest);

    UserResponse getUserById(Integer id);

    UserResponse getProfile();

    boolean updateProfile(UserRequest userRequest);

    Boolean changePassword(UserPassword userPassword);

    Boolean deleteUser(Integer id);
}
