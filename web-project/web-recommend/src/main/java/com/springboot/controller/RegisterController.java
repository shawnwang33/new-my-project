package com.springboot.controller;

import com.springboot.pojo.RegisterRequest;
import com.springboot.pojo.Result;
import com.springboot.service.RegisterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/register")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @PostMapping
    public Result register(@RequestBody RegisterRequest registerRequest) {
        log.info("注册请求: role={}, account={}", registerRequest.getRole(), registerRequest.getAccount());

        if (registerRequest.getRole() == null || registerRequest.getRole().trim().isEmpty()) {
            return Result.error("角色不能为空");
        }
        if (!"user".equals(registerRequest.getRole()) && !"admin".equals(registerRequest.getRole())) {
            return Result.error("角色参数错误");
        }
        if (registerRequest.getName() == null || registerRequest.getName().trim().isEmpty()) {
            return Result.error("名称不能为空");
        }
        if (registerRequest.getAccount() == null || registerRequest.getAccount().trim().isEmpty()) {
            return Result.error("账号不能为空");
        }
        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        String errorMsg = registerService.register(registerRequest);
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }
        return Result.success();
    }
}
