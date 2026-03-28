package com.springboot.service;

import com.springboot.pojo.NewsArticle;
import com.springboot.pojo.NewsCategory;
import com.springboot.pojo.NewsPageResult;

import java.util.List;

public interface NewsService {
    List<NewsCategory> listCategories();

    NewsPageResult listRecommendNews(Long userId, Long categoryId, String keyword, Integer page, Integer pageSize);

    List<NewsArticle> listHotNews();

    NewsArticle getNewsDetailById(Long id, Long userId);
}
