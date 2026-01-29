package com.hrd.asset_holder_api.controller;

import com.hrd.asset_holder_api.model.response.ApiResponse;
import com.hrd.asset_holder_api.model.response.LoginReq;
import com.hrd.asset_holder_api.model.response.LoginRes;
import com.hrd.asset_holder_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;

@Controller
@RequestMapping("/rest/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;


    @ResponseBody
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ResponseEntity<ApiResponse<LoginRes>> login(@Valid @RequestBody LoginReq  authRequest) throws Exception {
//        System.out.println(authService.login(authRequest));;
        ApiResponse<LoginRes>  res = ApiResponse.<LoginRes>builder()
                .message("Login successful")
                .payload(authService.login(authRequest))
                .httpStatus(HttpStatus.OK)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();
//        return ResponseEntity.status(HttpStatus.OK).body(authService.login(authRequest));
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}