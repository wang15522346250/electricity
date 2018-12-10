package com.atguigu.gmall.order.task;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.service.OrderInfoService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheServiceImpl implements InitializingBean, CacheService {

    /**
     * 本地加载缓存
     */
    private LoadingCache<String, List<OrderInfo>> loadingCache;

    /**
     * 订单服务
     */
    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * 更新缓存读写锁
     */
    private ReadWriteLock readWriteLock=new ReentrantReadWriteLock();

    /**
     * 获取缓存
     */
    private CacheLoader<String, List<OrderInfo>> cacheLoader;

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() {
        Lock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            cacheLoader = new CacheLoader<String, List<OrderInfo>>() {
                @Override
                public List<OrderInfo> load(String userId) throws Exception {
                    return orderInfoService.getOrderInfoList(userId);
                }
            };
        } catch (Exception e) {
            System.out.println("loggerUtil.warn  打印日志");
        }finally {
            readLock.unlock();
        }

        loadingCache = CacheBuilder
                .newBuilder()
                //cache最大数量
                .maximumSize(5000)
                //并发等级
                .concurrencyLevel(8)
                //初始化容量
                .initialCapacity(10)
                //刷新缓存时间
                .refreshAfterWrite(60 * 60, TimeUnit.SECONDS)
                //缓存命中数
                .recordStats()
                .removalListener(new RemovalListenerImpl())
                .build(cacheLoader);

    }

    @Override
    public List<OrderInfo> queryOrderInfoByCache(String userId) {
        try {
            return cacheLoader.load(userId);
        } catch (Exception e) {
            throw new RuntimeException("");
        }
    }
}
