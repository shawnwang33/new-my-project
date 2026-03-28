package com.springboot.pojo;

import lombok.Data;

import java.util.List;

@Data
public class CollectBatchCancelRequest {
    private Long userId;
    private List<Long> newsIds;
}
