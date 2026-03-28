package com.springboot.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CollectArticleVO {
    private Integer collectId;
    private Long userId;
    private Long newsId;
    private LocalDateTime collectTime;

    private String title;
    private String summary;
    private String coverUrl;
    private String source;
    private String categoryName;
    private LocalDateTime publishTime;
}
