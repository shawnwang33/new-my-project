package com.springboot.service;

import com.springboot.pojo.CommentRequest;
import com.springboot.pojo.NewsComment;

import java.util.List;

public interface CommentService {
    List<NewsComment> listByNewsId(Long newsId);

    Integer countByNewsId(Long newsId);

    String addComment(CommentRequest request);
}
