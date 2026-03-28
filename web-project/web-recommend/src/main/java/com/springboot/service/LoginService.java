package com.springboot.service;

import com.springboot.pojo.LoginRequest;
import com.springboot.pojo.LoginUserVO;

public interface LoginService {
    LoginUserVO login(LoginRequest loginRequest);
}
