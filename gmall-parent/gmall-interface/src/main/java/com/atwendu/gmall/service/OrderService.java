package com.atwendu.gmall.service;

import com.atwendu.gmall.bean.OrderInfo;
import com.atwendu.gmall.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {

    /**
     * 保存订单
     * @param orderInfo
     * @return
     */
    String saveOrder(OrderInfo orderInfo);

    /**
     * 生成流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 验证流水号
     * @param userId 获取缓存的流水号
     * @param tradeCodeNo 页面的的流水号
     * @return
     */
    boolean checkTradeCode(String userId,String tradeCodeNo);

    /**
     * 删除流水号
     * @param userId
     */
    void delTradeCode(String userId);

    /**
     * 查询是否有足够的库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId, Integer skuNum);

    /**
     * 根据orderId查询订单对象
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);

    /**
     * 更新订单状态
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(String orderId, ProcessStatus processStatus);

    /**
     * 发送消息给库存
     * @param orderId
     */
    void sendOrderStatus(String orderId);

    /**
     * 查询过期订单
     * @return
     */
    List<OrderInfo> getExpiredOrderList();

    /**
     * 出来过期订单
     * @param orderInfo
     */
    void execExpiredOrder(OrderInfo orderInfo);

    /**
     * 将 orderInfo 转换为map
     * @param orderInfo
     * @return
     */
    Map initWareOrder(OrderInfo orderInfo);

    /**
     * 拆单接口
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);
}
