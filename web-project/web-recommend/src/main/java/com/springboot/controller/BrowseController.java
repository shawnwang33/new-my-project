package com.springboot.controller;

import com.springboot.pojo.BrowseClearRequest;
import com.springboot.pojo.BrowseHistoryVO;
import com.springboot.pojo.Result;
import com.springboot.service.BrowseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/browse")
public class BrowseController {

    @Autowired
    private BrowseService browseService;

    @GetMapping("/list")
    public Result list(Long userId, String keyword, Boolean ascOrder) {
        if (userId == null) {
            return Result.error("userId不能为空");
        }
        List<BrowseHistoryVO> list = browseService.listByUser(userId, keyword, ascOrder);
        return Result.success(list);
    }

    @PostMapping("/clear")
    public Result clear(@RequestBody BrowseClearRequest request) {
        if (request.getUserId() == null) {
            return Result.error("userId不能为空");
        }
        Integer affected = browseService.clearByUser(request.getUserId());
        log.info("清空历史记录, userId={}, affected={}", request.getUserId(), affected);
        return Result.success(affected);
    }
}
