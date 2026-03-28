package com.springboot.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticle {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverUrl;
    private String source;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime publishTime;
    private Integer viewCount;
    private Integer recommendScore;
    private Integer status;
}
