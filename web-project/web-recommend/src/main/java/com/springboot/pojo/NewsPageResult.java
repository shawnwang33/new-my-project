package com.springboot.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsPageResult {
    private List<NewsArticle> list;
    private Long total;
    private Integer page;
    private Integer pageSize;
}
