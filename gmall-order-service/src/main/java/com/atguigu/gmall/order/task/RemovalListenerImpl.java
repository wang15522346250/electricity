package com.atguigu.gmall.order.task;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class RemovalListenerImpl implements RemovalListener {
    @Override
    public void onRemoval(RemovalNotification removalNotification) {
        System.out.println("缓存移除 removalNotification = " + removalNotification.getValue());
    }
}
