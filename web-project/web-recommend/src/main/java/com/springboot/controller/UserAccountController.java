package com.springboot.controller;

import com.springboot.pojo.ChangePasswordRequest;
import com.springboot.pojo.Result;
import com.springboot.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class UserAccountController {

    @Autowired
    private UserAccountService userAccountService;

    @PostMapping("/change-password")
    public Result changePassword(@RequestBody ChangePasswordRequest request) {
        String err = userAccountService.changePassword(request);
        if (err != null) {
            return Result.error(err);
        }
        return Result.success();
    }
}
