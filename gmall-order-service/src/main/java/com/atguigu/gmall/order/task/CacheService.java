package com.atguigu.gmall.order.task;

import com.atguigu.gmall.bean.OrderInfo;

import java.util.List;

public interface CacheService {
    List<OrderInfo> queryOrderInfoByCache(String userId);
}
