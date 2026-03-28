package com.springboot.service.impl;

import com.springboot.mapper.LoginMapper;
import com.springboot.pojo.ChangePasswordRequest;
import com.springboot.pojo.User;
import com.springboot.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private static final Pattern BCRYPT_PATTERN =
            Pattern.compile("^\\$2([aby])?\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private LoginMapper loginMapper;

    @Override
    public String changePassword(ChangePasswordRequest request) {
        if (request.getUserId() == null) {
            return "userId不能为空";
        }
        if (isBlank(request.getOldPassword())) {
            return "旧密码不能为空";
        }
        if (isBlank(request.getNewPassword())) {
            return "新密码不能为空";
        }
        if (request.getNewPassword().length() < 6) {
            return "新密码至少6位";
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            return "新密码不能与旧密码相同";
        }

        User user = loginMapper.findUserById(request.getUserId());
        if (user == null) {
            return "用户不存在";
        }
        if (!passwordMatched(request.getOldPassword(), user.getPassword())) {
            return "旧密码错误";
        }

        String encoded = passwordEncoder.encode(request.getNewPassword());
        loginMapper.updateUserPasswordById(request.getUserId(), encoded);
        return null;
    }

    private boolean passwordMatched(String rawPassword, String storedPassword) {
        if (isBlank(storedPassword)) {
            return false;
        }
        if (BCRYPT_PATTERN.matcher(storedPassword).matches()) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
