package com.springboot.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserVO {
    private Long id;
    private String name;
    private String account;
    private String avatar;
    private String role;
    private String token;
}
