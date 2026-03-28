package com.springboot.controller;

import com.springboot.pojo.CollectRequest;
import com.springboot.pojo.CollectBatchCancelRequest;
import com.springboot.pojo.CollectArticleVO;
import com.springboot.pojo.CollectStatusVO;
import com.springboot.pojo.Result;
import com.springboot.service.CollectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/collect")
public class CollectController {

    @Autowired
    private CollectService collectService;

    @GetMapping("/status")
    public Result status(Long userId, Long newsId) {
        if (newsId == null) {
            return Result.error("newsId不能为空");
        }
        CollectStatusVO data = collectService.getStatus(userId, newsId);
        return Result.success(data);
    }

    @PostMapping("/toggle")
    public Result toggle(@RequestBody CollectRequest request) {
        if (request.getUserId() == null) {
            return Result.error("userId不能为空");
        }
        if (request.getNewsId() == null) {
            return Result.error("newsId不能为空");
        }
        log.info("切换收藏, userId={}, newsId={}", request.getUserId(), request.getNewsId());
        CollectStatusVO data = collectService.toggle(request.getUserId(), request.getNewsId());
        return Result.success(data);
    }

    @GetMapping("/list")
    public Result list(Long userId, String keyword) {
        if (userId == null) {
            return Result.error("userId不能为空");
        }
        List<CollectArticleVO> list = collectService.listByUser(userId, keyword);
        return Result.success(list);
    }

    @PostMapping("/batch-cancel")
    public Result batchCancel(@RequestBody CollectBatchCancelRequest request) {
        if (request.getUserId() == null) {
            return Result.error("userId不能为空");
        }
        if (request.getNewsIds() == null || request.getNewsIds().isEmpty()) {
            return Result.error("请选择要取消收藏的新闻");
        }
        Integer affected = collectService.batchCancel(request.getUserId(), request.getNewsIds());
        return Result.success(affected);
    }
}
