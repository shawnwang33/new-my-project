package com.springboot.controller;

import com.springboot.pojo.NewsArticle;
import com.springboot.pojo.NewsCategory;
import com.springboot.pojo.NewsPageResult;
import com.springboot.pojo.Result;
import com.springboot.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping("/categories")
    public Result listCategories() {
        log.info("查询新闻分类");
        List<NewsCategory> categories = newsService.listCategories();
        return Result.success(categories);
    }

    @GetMapping("/recommend")
    public Result listRecommendNews(Long userId, Long categoryId, String keyword, Integer page, Integer pageSize) {
        log.info("查询推荐新闻, userId={}, categoryId={}, keyword={}, page={}, pageSize={}", userId, categoryId, keyword, page, pageSize);
        NewsPageResult pageResult = newsService.listRecommendNews(userId, categoryId, keyword, page, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/hot")
    public Result listHotNews() {
        log.info("查询热门新闻");
        List<NewsArticle> hotList = newsService.listHotNews();
        return Result.success(hotList);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id, Long userId) {
        log.info("查询新闻详情, id={}, userId={}", id, userId);
        NewsArticle detail = newsService.getNewsDetailById(id, userId);
        if (detail == null) {
            return Result.error("新闻不存在或已下线");
        }
        return Result.success(detail);
    }

    @GetMapping("/home")
    public Result home(Long categoryId, String keyword) {
        Map<String, Object> data = new HashMap<>();
        data.put("categories", newsService.listCategories());
        data.put("recommendNews", newsService.listRecommendNews(null, categoryId, keyword, 1, 10));
        data.put("hotNews", newsService.listHotNews());
        return Result.success(data);
    }
}
