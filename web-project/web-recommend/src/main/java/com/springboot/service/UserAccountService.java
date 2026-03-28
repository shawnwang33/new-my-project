package com.springboot.service;

import com.springboot.pojo.ChangePasswordRequest;

public interface UserAccountService {
    String changePassword(ChangePasswordRequest request);
}
