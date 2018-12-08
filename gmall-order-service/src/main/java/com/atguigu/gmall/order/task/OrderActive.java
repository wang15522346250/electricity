package com.atguigu.gmall.order.task;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

@Component
@EnableScheduling
public class OrderActive {

    @Autowired
    OrderInfoService orderInfoService;

    private ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "orderActive");
                }
            });

    @Scheduled(cron = "0/50 * * * * ?")
    public void checkOrderExpireInfo() {
        //获取过期订单的集合
        System.out.println("开始扫描过期时间");
        long start = System.currentTimeMillis();
        List<OrderInfo> orderInfoExpireList = orderInfoService.getOrderInfoExpireList();
        for (OrderInfo orderInfo : orderInfoExpireList) {
            System.out.println("扫描完成");
            orderInfoService.setOrderStatus(orderInfo);

        }
        long time = System.currentTimeMillis() - start;
        System.out.println("一共扫描了" + orderInfoExpireList.size() + "个订单,更新时间为：" + time + "豪秒");
    }

    /**
     * 第二种调用定时方案  只需调用该方法即可
     */
    public void checkorderExpireInfoByScheduled() {
        //延时推送检查  第一次1小时检查，第二次/以后每十二小时检查一次
        executorService.
                scheduleAtFixedRate(new OrderActiveThread(), 1, 12, TimeUnit.HOURS);
    }

    class OrderActiveThread implements Runnable {

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            System.out.println("开始扫描时间 startTime = " + startTime);
            List<OrderInfo> orderInfoExpireList = orderInfoService.getOrderInfoExpireList();
            for (OrderInfo orderInfo : orderInfoExpireList) {
                System.out.println("扫描完成");
                orderInfoService.setOrderStatus(orderInfo);
            }
            long endTime = System.currentTimeMillis();
            System.out.println("扫描用了" + (endTime - startTime) + " 毫秒");
        }
    }

    public void setOrderInfoService(OrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }
}
