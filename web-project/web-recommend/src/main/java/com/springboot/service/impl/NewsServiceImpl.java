package com.springboot.service.impl;

import com.springboot.mapper.NewsMapper;
import com.springboot.pojo.NewsArticle;
import com.springboot.pojo.NewsCategory;
import com.springboot.pojo.NewsPageResult;
import com.springboot.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsMapper newsMapper;

    @Override
    public List<NewsCategory> listCategories() {
        return newsMapper.listCategories();
    }

    @Override
    public NewsPageResult listRecommendNews(Long userId, Long categoryId, String keyword, Integer page, Integer pageSize) {
        int currentPage = (page == null || page < 1) ? 1 : page;
        int currentPageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        int offset = (currentPage - 1) * currentPageSize;

        if (userId != null && userId > 0) {
            Long personalizedTotal = newsMapper.countPersonalizedRecommendNews(userId, categoryId, keyword);
            if (personalizedTotal != null && personalizedTotal > 0) {
                List<NewsArticle> personalizedList = newsMapper.listPersonalizedRecommendNewsPage(userId, categoryId, keyword, offset, currentPageSize);
                return new NewsPageResult(personalizedList, personalizedTotal, currentPage, currentPageSize);
            }
        }

        Long total = newsMapper.countRecommendNews(categoryId, keyword);
        List<NewsArticle> list = newsMapper.listRecommendNewsPage(categoryId, keyword, offset, currentPageSize);
        return new NewsPageResult(list, total, currentPage, currentPageSize);
    }

    @Override
    public List<NewsArticle> listHotNews() {
        return newsMapper.listHotNews();
    }

    @Override
    public NewsArticle getNewsDetailById(Long id, Long userId) {
        newsMapper.increaseViewCount(id);
        Integer nextBrowseId = newsMapper.nextBrowseId();
        newsMapper.insertBrowse(
                nextBrowseId == null ? 1 : nextBrowseId,
                userId == null ? 0L : userId,
                id,
                LocalDateTime.now()
        );
        NewsArticle detail = newsMapper.getNewsDetailById(id);
        if (detail == null) {
            return null;
        }
        if (detail.getContent() == null || detail.getContent().trim().isEmpty()) {
            detail.setContent(detail.getSummary() == null ? "" : detail.getSummary());
        }
        return detail;
    }
}
