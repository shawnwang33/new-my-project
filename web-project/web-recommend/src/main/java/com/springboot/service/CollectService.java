package com.springboot.service;

import com.springboot.pojo.CollectStatusVO;
import com.springboot.pojo.CollectArticleVO;

import java.util.List;

public interface CollectService {
    CollectStatusVO getStatus(Long userId, Long newsId);

    CollectStatusVO toggle(Long userId, Long newsId);

    List<CollectArticleVO> listByUser(Long userId, String keyword);

    Integer batchCancel(Long userId, List<Long> newsIds);
}
