package com.springboot.controller;

import com.springboot.pojo.LoginRequest;
import com.springboot.pojo.LoginUserVO;
import com.springboot.pojo.Result;
import com.springboot.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping
    public Result login(@RequestBody LoginRequest loginRequest) {
        log.info("登录请求: role={}, account={}", loginRequest.getRole(), loginRequest.getAccount());

        if (loginRequest.getRole() == null || loginRequest.getRole().trim().isEmpty()) {
            return Result.error("角色不能为空");
        }
        if (!"user".equals(loginRequest.getRole()) && !"admin".equals(loginRequest.getRole())) {
            return Result.error("角色参数错误");
        }
        if (loginRequest.getAccount() == null || loginRequest.getAccount().trim().isEmpty()) {
            return Result.error("账号不能为空");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        LoginUserVO loginUser = loginService.login(loginRequest);
        if (loginUser == null) {
            return Result.error("账号或密码错误");
        }
        return Result.success(loginUser);
    }
}
