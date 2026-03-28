package com.springboot.service.impl;

import com.springboot.mapper.CommentMapper;
import com.springboot.pojo.CommentRequest;
import com.springboot.pojo.NewsComment;
import com.springboot.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public List<NewsComment> listByNewsId(Long newsId) {
        return commentMapper.listByNewsId(newsId);
    }

    @Override
    public Integer countByNewsId(Long newsId) {
        return commentMapper.countByNewsId(newsId);
    }

    @Override
    public String addComment(CommentRequest request) {
        if (request.getNewsId() == null) {
            return "新闻ID不能为空";
        }
        if (request.getUserAccount() == null || request.getUserAccount().trim().isEmpty()) {
            return "评论用户不能为空";
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return "评论内容不能为空";
        }
        if (request.getContent().trim().length() > 300) {
            return "评论内容不能超过300字";
        }

        NewsComment comment = new NewsComment();
        comment.setUserId(resolveUserId(request.getUserAccount()));
        comment.setNewsId(request.getNewsId());
        comment.setUserAccount(request.getUserAccount().trim());
        comment.setContent(request.getContent().trim());
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        return null;
    }

    private Long resolveUserId(String userAccount) {
        // 当前项目评论模块不依赖用户中心，先做默认映射，后续可接用户表查询
        if ("admin".equalsIgnoreCase(userAccount)) {
            return 9001L;
        }
        return 10001L;
    }
}
