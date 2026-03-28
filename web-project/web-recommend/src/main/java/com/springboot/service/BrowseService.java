package com.springboot.service;

import com.springboot.pojo.BrowseHistoryVO;

import java.util.List;

public interface BrowseService {
    List<BrowseHistoryVO> listByUser(Long userId, String keyword, Boolean ascOrder);

    Integer clearByUser(Long userId);
}
