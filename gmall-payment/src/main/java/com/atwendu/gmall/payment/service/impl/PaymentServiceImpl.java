package com.atwendu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atwendu.gmall.bean.OrderInfo;
import com.atwendu.gmall.bean.PaymentInfo;
import com.atwendu.gmall.bean.enums.PaymentStatus;
import com.atwendu.gmall.bean.enums.ProcessStatus;
import com.atwendu.gmall.config.ActiveMQUtil;
import com.atwendu.gmall.payment.mapper.PaymentInfoMapper;
import com.atwendu.gmall.service.OrderService;
import com.atwendu.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private AlipayClient alipayClient;

    @Reference
    private OrderService orderService;

    @Autowired
    private ActiveMQUtil activeMQUtil;


    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {
        //select * from paymentInfo where out_trade_no = ?
        //select * from paymentInfo where orderId = ? orderId
        return paymentInfoMapper.selectOne(paymentInfoQuery);
    }

    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfo) {
        //更新
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    @Override
    public boolean refund(String orderId) {

        //通过orderId获取数据
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("refund_amount",orderInfo.getTotalAmount());
        map.put("refund_reason","不买了");

        request.setBizContent(JSON.toJSONString(map));

//        JSONObject bizContent = new JSONObject();
//        bizContent.put("trade_no", "2021081722001419121412730660");
//        bizContent.put("refund_amount", 0.01);
//        bizContent.put("out_request_no", "HZ01RF001");

        //// 返回参数选项，按需传入
        //JSONArray queryOptions = new JSONArray();
        //queryOptions.add("refund_detail_item_list");
        //bizContent.put("query_options", queryOptions);

//        request.setBizContent(bizContent.toString());

        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            //更新状态
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }

    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {

        //创建连接
        Connection connection = activeMQUtil.getConnection();

        try {
            //打开连接
            connection.start();
            //创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue payment_result_queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            //创建消息提供者
            MessageProducer producer = session.createProducer(payment_result_queue);

            //创建消息对象
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("orderId",paymentInfo.getOrderId());
            activeMQMapMessage.setString("result",result);

            //发送消息
            producer.send(activeMQMapMessage);

            //提交
            session.commit();
            closeAll(connection, session, producer);

        } catch (JMSException e) {

        }
    }

    // 查询支付交易是否成功！需要根据orderId 查询！
    // http://payment.gmall.com/query?orderId=?
    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {

//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        if (paymentInfoQuery.getPaymentStatus().equals(ProcessStatus.PAID) || paymentInfoQuery.getPaymentStatus().equals(ProcessStatus.CLOSED)){
            return true;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfoQuery.getOutTradeNo());
        request.setBizContent(JSON.toJSONString(map));

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "20150320010101001");
//bizContent.put("trade_no", "2014112611001004680073956707");
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            // 表示有支付记录：
            if ("TRADE_SUCCESS".equals(response.getTradeStatus()) || "TRADE_FINISHED".equals(response.getTradeStatus()) ){
                // 支付成功！
                // 更新状态
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfoQuery.getOutTradeNo(),paymentInfoUpd);
                // 通知订单支付完成了！
                sendPaymentResult(paymentInfoQuery,"success");
                return true;
            }
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }

        return false;
    }

    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {

        // 创建工厂
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            // 创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payment_result_check_queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");

            MessageProducer producer = session.createProducer(payment_result_check_queue);
            // 创建消息对象
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("outTradeNo",outTradeNo);
            activeMQMapMessage.setInt("delaySec",delaySec);
            activeMQMapMessage.setInt("checkCount",checkCount);

            // 设置延迟队列的开启
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);
            producer.send(activeMQMapMessage);

            // 提交
            session.commit();

            // 关闭
            closeAll(connection,session,producer);

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void closePayment(String orderId) {
        // 更新状态：
        // 第一个参数：表示要更新的值
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);

        // 第二个参数：example 按照什么条件更新
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);

        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    /**
     *
     * @param connection
     * @param session
     * @param producer
     * @throws JMSException
     */
    public void closeAll(Connection connection, Session session, MessageProducer producer) throws JMSException {
        //关闭
        producer.close();
        session.close();
        connection.close();
    }
}
