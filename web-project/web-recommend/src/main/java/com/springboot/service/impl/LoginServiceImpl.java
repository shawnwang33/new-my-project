package com.springboot.service.impl;

import com.springboot.mapper.LoginMapper;
import com.springboot.pojo.Admin;
import com.springboot.pojo.LoginRequest;
import com.springboot.pojo.LoginUserVO;
import com.springboot.pojo.User;
import com.springboot.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class LoginServiceImpl implements LoginService {

    private static final Pattern BCRYPT_PATTERN =
            Pattern.compile("^\\$2([aby])?\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private LoginMapper loginMapper;

    @Override
    public LoginUserVO login(LoginRequest loginRequest) {
        if ("admin".equals(loginRequest.getRole())) {
            Admin admin = loginMapper.findAdminByAccount(loginRequest.getAccount());
            if (admin == null || !isPasswordMatched(loginRequest.getPassword(), admin.getPassword(), true, admin.getAdminId())) {
                return null;
            }
            return new LoginUserVO(
                    admin.getAdminId(),
                    admin.getAdminName(),
                    admin.getAdminAccount(),
                    null,
                    "admin",
                    UUID.randomUUID().toString().replace("-", "")
            );
        }

        User user = loginMapper.findUserByAccount(loginRequest.getAccount());
        if (user == null || !isPasswordMatched(loginRequest.getPassword(), user.getPassword(), false, user.getUserId())) {
            return null;
        }
        return new LoginUserVO(
                user.getUserId(),
                user.getUserName(),
                user.getUserAccount(),
                user.getAvatar(),
                "user",
                UUID.randomUUID().toString().replace("-", "")
        );
    }

    private boolean isPasswordMatched(String rawPassword, String storedPassword, boolean admin, Long id) {
        if (storedPassword == null || storedPassword.trim().isEmpty()) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        if (!storedPassword.equals(rawPassword)) {
            return false;
        }

        // 兼容历史明文密码：首次登录成功后自动升级为 BCrypt 密文
        String encodedPassword = passwordEncoder.encode(rawPassword);
        if (admin) {
            loginMapper.updateAdminPasswordById(id, encodedPassword);
        } else {
            loginMapper.updateUserPasswordById(id, encodedPassword);
        }
        return true;
    }

    private boolean isBcrypt(String password) {
        return BCRYPT_PATTERN.matcher(password).matches();
    }
}
