package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderInfoService;
import com.atguigu.gmall.service.PaymentService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class OrderInfoServiceImpl implements OrderInfoService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Reference
    PaymentService paymentService;

    private String ORDER_PREFIX = "order:";
    private String ORDER_SUFFIX = ":code";
    private int TIME_OUT = 60 * 60;
    private  String ORDER_INFO_PREFIX="order";
    private  String ORDER_INFO_SUFFIX=":info";

    @Override
    public String getUniquIdentifier(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeKey = ORDER_PREFIX + userId + ORDER_SUFFIX;
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeCodeKey, TIME_OUT, tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public Boolean checkTradeCode(String userId, String tradeCode) {
        String tradeCodeKey = ORDER_PREFIX + userId + ORDER_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeRedis = jedis.get(tradeCodeKey);
        jedis.close();
        if (tradeCodeRedis != null && tradeCodeRedis.length() > 0) {
            if (tradeCodeRedis.equals(tradeCode)) {
                return true;
            } else {
                return false;
            }
        }
        return false;

    }

    @Override
    public void deleteTradeCode(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeKey = ORDER_PREFIX + userId + ORDER_SUFFIX;
        jedis.del(tradeCodeKey);
        jedis.close();
    }

    @Override
    public String save(OrderInfo orderInfo) {
        //设置订单创建时间
        orderInfo.setCreateTime(new Date());
        //日期相对于增加一天
        //设置订单过期时间
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DATE,1);

        orderInfo.setExpectDeliveryTime(calendar.getTime());

        //设置tradeNo
        String outTradeNo = "ORDER_TRADE" + UUID.randomUUID().toString() + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //先加入oredrInfo订单表
        orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        String orderId=orderInfo.getId();
        return orderId;
    }

    @Override
    //获取订单信息详情
    public OrderInfo getOrderInfo(String orederId) {
        //获取orderInfo信息
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orederId);
        OrderDetail orderDetail=new OrderDetail();
        orderDetail.setOrderId(orederId);
        //根据订单信息id,查询订单详细列表集合
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        //把查询中的集合封装到orderInfo中
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }

    //获取orderInfo的信息
    @Override
    public List<OrderInfo> getOrderInfoList(String userId) {
        //遍历查询耗性能
      /*  OrderInfo orderInfoQuery=new OrderInfo();
        orderInfoQuery.setUserId(userId);
        List<OrderInfo> orderInfoList = orderInfoMapper.select(orderInfoQuery);
        OrderDetail orderDetail=new OrderDetail();
        for (OrderInfo orderInfo : orderInfoList) {
            orderDetail.setOrderId(orderInfo.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
            orderInfo.setOrderDetailList(orderDetailList);
        }*/
      //两表关联查询
       List<OrderInfo>orderInfoList= orderInfoMapper.selectOrderInfoListByUserId(Long.parseLong(userId));
        return orderInfoList;
    }
    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus){
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        //设置进程状态
        orderInfo.setProcessStatus(processStatus);
        //更新订单状态
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        //更新进程状态
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public void sendOrderResult(String orderId) {
        //获取提供者
        //1.获取工厂连接
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        //2.创建连接
        try {
            Connection connection = connectionFactory.createConnection();
            //3.开启连接
            connection.start();
            //创建带有事务的session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建消息队列
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            //设置发送消息队列的类型
            TextMessage textMessage=new ActiveMQTextMessage();
           //获取访问仓库类型的json
            String mapJson = getMapJson(orderId);
            //设置要发送的文本信息
            textMessage.setText(mapJson);
            //创建生产者
            MessageProducer producer = session.createProducer(order_result_queue);
            //发送信息
            producer.send(textMessage);
            //提交事务
            session.commit();
            session.close();
            connection.close();
            producer.close();
            //纵向代理  横向抽取  13120287773
            //动态数据源
            //动态代理
            //动态代理：字节码重组
            //NIO 1.5 BIO AIO1.7   1000万条数写入数据库中怎么实现
            //一次性hash值
            //netty
            //springbean的生命周期

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getMapJson(String orderId){
        Map<String, Object> map= getMap(orderId);
        String details = JSON.toJSONString(map);
        return details;
    }
    @Override
    public Map<String,Object> getMap(String orderId){
        Map<String,Object> map=new HashMap<>();
        OrderInfo orderInfo = getOrderInfo(orderId);
        map.put("orderId",orderId);
        map.put("consignee",orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody",orderInfo.getTradeBody());
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");

        List<Map> mapList=new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map<String,Object>orderDetailMap=new HashMap<>();
            orderDetailMap.put("skuId",orderDetail.getSkuId());
            orderDetailMap.put("skuNum",orderDetail.getSkuNum());
            orderDetailMap.put("skuName",orderDetail.getSkuName());
            mapList.add(orderDetailMap);
        }
        map.put("details",mapList);
        return map;
    }
    //获取过期订单时间
    @Override
    public List<OrderInfo> getOrderInfoExpireList(){
        Example example=new Example(OrderInfo.class);
        example.createCriteria().andLessThan("expectDeliveryTime",new Date()).andEqualTo("processStatus",ProcessStatus.UNPAID);
        List<OrderInfo> orderInfoList = orderInfoMapper.selectByExample(example);
        return orderInfoList;

    }
    @Override
    @Async
    public void setOrderStatus(OrderInfo orderInfo){
        //更新订单状态
        updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);
        //关闭支付状态
        paymentService.closePaymentStatus(orderInfo.getId());
    }

    @Override
    //拆分订单
    public List<OrderInfo> splitOrder(String orderId, String wareSkuMap) {
        //1.获取订单信息
        OrderInfo orderInfo = getOrderInfo(orderId);
        //获取订单详情集合
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //2.把paramMap反序列化
        List<Map>mapList=JSON.parseArray(wareSkuMap,Map.class);

        //创建一个新的订单集合
        List<OrderInfo> orderInfoList=new ArrayList<>();

        //3.遍历查询出mapList中的元素
        for (Map map : mapList) {
            String wareId =(String) map.get("wareId");
            //创建一个新的订单
            OrderInfo newOrderInfo=new OrderInfo();
            try {
                //拷贝订单信息，把原订单信息拷贝到新订单中
                BeanUtils.copyProperties(newOrderInfo,orderInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            //给子订单设置父订单id
            newOrderInfo.setParentOrderId(orderInfo.getId());
            //设置仓库id
            newOrderInfo.setWareId(wareId);
            //设置id为主键自增
            newOrderInfo.setId(null);
            List<String> skuIds =(List<String>) map.get("skuIds");
            //创建一个新的orderDetail集合
            List<OrderDetail> newOrderDetailList=new ArrayList<>();
            for (String skuId : skuIds) {

                for (OrderDetail orderOrignalDetail : orderDetailList) {
                    //如果skuId相匹配
                    if (skuId.equals(orderOrignalDetail.getSkuId())){
                        //设置id为null,防止主键重复
                        orderOrignalDetail.setId(null);
                        newOrderDetailList.add(orderOrignalDetail);
                    }
                }

            }
            //设置子订单的总金额
            newOrderInfo.sumTotalAmount();
            newOrderInfo.setOrderDetailList(newOrderDetailList);
            //保存到数据库中
            save(newOrderInfo);
            //更新状态
            updateOrderStatus(orderInfo.getId(),ProcessStatus.SPLIT);
            orderInfoList.add(newOrderInfo);
        }


        return orderInfoList;
    }


}
