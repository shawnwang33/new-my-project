package com.springboot.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BrowseHistoryVO {
    private Integer recordId;
    private Long userId;
    private Long newsId;
    private LocalDateTime browseTime;

    private String title;
    private String summary;
    private String coverUrl;
    private String source;
    private String categoryName;
    private LocalDateTime publishTime;
}
