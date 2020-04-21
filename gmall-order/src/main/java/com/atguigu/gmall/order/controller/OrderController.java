package com.atguigu.gmall.order.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.config.AlipayTemplate;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.PayAsyncVo;
import com.atguigu.gmall.order.vo.PayVo;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @GetMapping("confirm")
    public Resp<OrderConfirmVo> confirm(){
        OrderConfirmVo orderConfirmVo = orderService.confirm();
        return Resp.ok(orderConfirmVo);
    }

    @PostMapping("submit")
    public Resp<OrderEntity> submit(@RequestBody OrderSubmitVo orderSubmitVo){

        OrderEntity orderEntity = orderService.submit(orderSubmitVo);
        if (orderEntity != null){
            PayVo payVo = new PayVo();
            payVo.setOut_trade_no(orderEntity.getOrderSn());
            payVo.setTotal_amount(orderEntity.getTotalAmount().toString());
            payVo.setSubject("谷粒商城");
            payVo.setBody("谷粒商城支付平台");
            try {
                String form = alipayTemplate.pay(payVo);
                System.out.println(form);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
        }
        return Resp.ok(orderEntity);
    }

    @PostMapping("pay/success")
    public Resp<Object> paySuccess(PayAsyncVo payAsyncVo){

        amqpTemplate.convertAndSend("ORDER-EXCHANGE","order.pay",payAsyncVo.getOut_trade_no());

        return Resp.ok(null);
    }
}
