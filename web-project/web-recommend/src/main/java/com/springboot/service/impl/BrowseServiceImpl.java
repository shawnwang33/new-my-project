package com.springboot.service.impl;

import com.springboot.mapper.BrowseMapper;
import com.springboot.pojo.BrowseHistoryVO;
import com.springboot.service.BrowseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrowseServiceImpl implements BrowseService {

    @Autowired
    private BrowseMapper browseMapper;

    @Override
    public List<BrowseHistoryVO> listByUser(Long userId, String keyword, Boolean ascOrder) {
        Long actualUserId = userId == null ? 0L : userId;
        return browseMapper.listByUser(actualUserId, keyword, ascOrder);
    }

    @Override
    public Integer clearByUser(Long userId) {
        Long actualUserId = userId == null ? 0L : userId;
        Integer affected = browseMapper.clearByUser(actualUserId);
        return affected == null ? 0 : affected;
    }
}
