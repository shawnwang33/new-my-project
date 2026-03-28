package com.springboot.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsComment {
    private Long id;
    private Long userId;
    private Long newsId;
    private String userAccount;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
}
