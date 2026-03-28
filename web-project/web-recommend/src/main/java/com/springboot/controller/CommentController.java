package com.springboot.controller;

import com.springboot.pojo.CommentRequest;
import com.springboot.pojo.NewsComment;
import com.springboot.pojo.Result;
import com.springboot.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/news/{newsId}")
    public Result listByNews(@PathVariable Long newsId) {
        log.info("查询评论列表, newsId={}", newsId);
        List<NewsComment> list = commentService.listByNewsId(newsId);
        Integer count = commentService.countByNewsId(newsId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("count", count);
        return Result.success(data);
    }

    @PostMapping
    public Result add(@RequestBody CommentRequest request) {
        log.info("发表评论, newsId={}, user={}", request.getNewsId(), request.getUserAccount());
        String errorMsg = commentService.addComment(request);
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }
        return Result.success();
    }
}
