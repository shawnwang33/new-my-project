package com.springboot.service.impl;

import com.springboot.mapper.RegisterMapper;
import com.springboot.pojo.Admin;
import com.springboot.pojo.RegisterRequest;
import com.springboot.pojo.User;
import com.springboot.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterServiceImpl implements RegisterService {

    private static final String DEFAULT_AVATAR = "https://dummyimage.com/64x64/e6f4ff/4c8bf5&text=U";

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private RegisterMapper registerMapper;

    @Override
    public String register(RegisterRequest registerRequest) {
        if ("admin".equals(registerRequest.getRole())) {
            if (registerMapper.countAdminByAccount(registerRequest.getAccount()) > 0) {
                return "管理员账号已存在";
            }
            if (registerMapper.countAdminByName(registerRequest.getName()) > 0) {
                return "管理员名称已存在";
            }

            Long adminId = registerMapper.maxAdminId() + 1;
            Admin admin = new Admin();
            admin.setAdminId(adminId);
            admin.setAdminName(registerRequest.getName());
            admin.setAdminAccount(registerRequest.getAccount());
            admin.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            registerMapper.insertAdmin(admin);
            return null;
        }

        if (registerMapper.countUserByAccount(registerRequest.getAccount()) > 0) {
            return "用户账号已存在";
        }
        if (registerMapper.countUserByName(registerRequest.getName()) > 0) {
            return "用户名已存在";
        }

        Long userId = registerMapper.maxUserId() + 1;
        User user = new User();
        user.setUserId(userId);
        user.setUserName(registerRequest.getName());
        user.setUserAccount(registerRequest.getAccount());
        user.setAvatar(DEFAULT_AVATAR);
        user.setIntroduction(registerRequest.getIntroduction());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        registerMapper.insertUser(user);
        return null;
    }
}
