package com.springboot.service.impl;

import com.springboot.mapper.CollectMapper;
import com.springboot.pojo.CollectArticleVO;
import com.springboot.pojo.CollectStatusVO;
import com.springboot.service.CollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollectServiceImpl implements CollectService {

    @Autowired
    private CollectMapper collectMapper;

    @Override
    public CollectStatusVO getStatus(Long userId, Long newsId) {
        Long actualUserId = userId == null ? 0L : userId;
        boolean collected = safe(collectMapper.existsByUserAndNews(actualUserId, newsId)) > 0;
        Integer count = safe(collectMapper.countByNewsId(newsId));
        return new CollectStatusVO(collected, count);
    }

    @Override
    public CollectStatusVO toggle(Long userId, Long newsId) {
        Long actualUserId = userId == null ? 0L : userId;
        boolean collected = safe(collectMapper.existsByUserAndNews(actualUserId, newsId)) > 0;
        if (collected) {
            collectMapper.deleteByUserAndNews(actualUserId, newsId);
        } else {
            Integer nextId = collectMapper.nextCollectId();
            collectMapper.insert(nextId == null ? 1 : nextId, actualUserId, newsId, LocalDateTime.now());
        }
        return getStatus(actualUserId, newsId);
    }

    @Override
    public List<CollectArticleVO> listByUser(Long userId, String keyword) {
        Long actualUserId = userId == null ? 0L : userId;
        return collectMapper.listByUser(actualUserId, keyword);
    }

    @Override
    public Integer batchCancel(Long userId, List<Long> newsIds) {
        Long actualUserId = userId == null ? 0L : userId;
        if (newsIds == null || newsIds.isEmpty()) {
            return 0;
        }
        List<Long> validNewsIds = newsIds.stream()
                .filter(item -> item != null && item > 0)
                .distinct()
                .collect(Collectors.toList());
        if (validNewsIds.isEmpty()) {
            return 0;
        }
        Integer affected = collectMapper.deleteBatchByUserAndNewsIds(actualUserId, validNewsIds);
        return affected == null ? 0 : affected;
    }

    private Integer safe(Integer value) {
        return value == null ? 0 : value;
    }
}
