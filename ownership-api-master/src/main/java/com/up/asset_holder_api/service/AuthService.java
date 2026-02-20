package com.up.asset_holder_api.service;


import com.up.asset_holder_api.model.response.LoginReq;
import com.up.asset_holder_api.model.response.LoginRes;

public interface AuthService {

    LoginRes login(LoginReq authRequest) throws Exception;


}
