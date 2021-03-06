package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderInfoService {
    String getUniquIdentifier(String userId);

    Boolean checkTradeCode(String userId, String tradeCode);

    void deleteTradeCode(String userId);

    String save(OrderInfo orderInfo);

    OrderInfo getOrderInfo(String orederId);

    public List<OrderInfo> getOrderInfoList(String userId);

    public void updateOrderStatus(String orderId, ProcessStatus processStatus);

    void sendOrderResult(String orderId);

    public List<OrderInfo> getOrderInfoExpireList();

    public void setOrderStatus(OrderInfo orderInfo);

    List<OrderInfo> splitOrder(String orderId, String paramMap);

    public String getMapJson(String orderId);

    public Map<String,Object> getMap(String orderId);
}
