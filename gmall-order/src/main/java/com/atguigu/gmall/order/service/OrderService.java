package com.atguigu.gmall.order.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.core.exception.OrderException;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallCartClient cartClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallOmsClient omsClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String TOKEN_PREFIX = "order:token:";



    public OrderConfirmVo confirm() {

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        UserInfo userInfo = LoginInterceptor.userInfo();

        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            Resp<List<MemberReceiveAddressEntity>> listResp = umsClient.queryAddressByUserId(userInfo.getUserId());
            List<MemberReceiveAddressEntity> addressEntities = listResp.getData();
            orderConfirmVo.setAddressEntities(addressEntities);
        }, threadPoolExecutor);

        CompletableFuture<Void> itemsFuture = CompletableFuture.supplyAsync(() -> {
            List<Cart> carts = cartClient.queryCheckedCarts(userInfo.getUserId());
            return carts;
        }).thenAcceptAsync(carts -> {
            List<OrderItemVo> orderItemVos = carts.stream().map(cart -> {
                Long skuId = cart.getSkuId();
                Integer count = cart.getCount();
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(skuId);
                orderItemVo.setCount(count);

                CompletableFuture<Void> skuFuture = CompletableFuture.runAsync(() -> {
                    Resp<SkuInfoEntity> skuInfoEntityResp = pmsClient.querySkuById(skuId);
                    SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                    if (skuInfoEntity != null) {
                        orderItemVo.setPrice(skuInfoEntity.getPrice());
                        orderItemVo.setImage(skuInfoEntity.getSkuDefaultImg());
                        orderItemVo.setSkuTitle(skuInfoEntity.getSkuTitle());
                        orderItemVo.setWeight(skuInfoEntity.getWeight());
                    }
                }, threadPoolExecutor);

                CompletableFuture<Void> storeFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<WareSkuEntity>> wareSkuBySkuIdResp = wmsClient.queryWareSkuBySkuId(skuId);
                    List<WareSkuEntity> wareSkuEntities = wareSkuBySkuIdResp.getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        orderItemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                    }
                }, threadPoolExecutor);

                CompletableFuture<Void> saleAttrFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = pmsClient.querySaleAttrValueBySkuId(skuId);
                    List<SkuSaleAttrValueEntity> saleAttrValueEntities = saleAttrValueResp.getData();
                    orderItemVo.setSaleAttrs(saleAttrValueEntities);
                }, threadPoolExecutor);

                CompletableFuture<Void> salesFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<ItemSaleVO>> itemSaleVoResp = smsClient.queryItemSaleVoBySkuId(skuId);
                    List<ItemSaleVO> itemSaleVOS = itemSaleVoResp.getData();
                    orderItemVo.setSales(itemSaleVOS);
                }, threadPoolExecutor);

                CompletableFuture.allOf(skuFuture, storeFuture, saleAttrFuture, salesFuture).join();
                return orderItemVo;
            }).collect(Collectors.toList());
            orderConfirmVo.setOrderItemVos(orderItemVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> boundsFuture = CompletableFuture.runAsync(() -> {
            Resp<MemberEntity> memberEntityResp = umsClient.queryMemberById(userInfo.getUserId());
            MemberEntity memberEntity = memberEntityResp.getData();
            if (memberEntity != null) {
                orderConfirmVo.setBounds(memberEntity.getIntegration());
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> tokenFuture = CompletableFuture.runAsync(() -> {
            String orderToken = IdWorker.getTimeId();
            orderConfirmVo.setOrderToken(orderToken);
            stringRedisTemplate.opsForValue().set(TOKEN_PREFIX + orderToken, orderToken, 3, TimeUnit.HOURS);
        }, threadPoolExecutor);

        CompletableFuture.allOf(addressFuture, itemsFuture, boundsFuture, tokenFuture).join();
        return orderConfirmVo;
    }

    public OrderEntity submit(OrderSubmitVo orderSubmitVo) {

        String orderToken = orderSubmitVo.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long flag = (Long) stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(TOKEN_PREFIX + orderSubmitVo.getOrderToken()), orderToken);
        if (flag == 0){
            throw new OrderException("请不要重复提交订单");
        }

        BigDecimal totalPrice = orderSubmitVo.getTotalPrice();
        List<OrderItemVo> items = orderSubmitVo.getItems();
        if (CollectionUtils.isEmpty(items)){
            throw new OrderException("请勾选要购买的商品");
        }
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            Long skuId = item.getSkuId();
            Resp<SkuInfoEntity> skuInfoEntityResp = pmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity != null) {
                return skuInfoEntity.getPrice().multiply(new BigDecimal(item.getCount()));
            }
            return new BigDecimal(0);
        }).reduce((a, b) -> a.add(b)).get();

        if (totalPrice.compareTo(currentTotalPrice) !=0){
            throw new OrderException("页面已过期，请刷新后重试");
        }

        List<SkuLockVo> skuLockVos = items.stream().map(orderItemVo -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setSkuId(orderItemVo.getSkuId());
            skuLockVo.setCount(orderItemVo.getCount());
            skuLockVo.setOrderToken(orderSubmitVo.getOrderToken());
            return skuLockVo;
        }).collect(Collectors.toList());
        Resp<List<SkuLockVo>> listResp = wmsClient.checkAndLock(skuLockVos);
        List<SkuLockVo> lockVos = listResp.getData();
        if (!CollectionUtils.isEmpty(lockVos)){
            throw new OrderException(JSON.toJSONString(lockVos));
        }

        UserInfo userInfo = LoginInterceptor.userInfo();
        OrderEntity orderEntity = null;
        try {
            Resp<OrderEntity> orderEntityResp = omsClient.saveOrder(orderSubmitVo, userInfo.getUserId());
            orderEntity = orderEntityResp.getData();
        } catch (Exception e) {
            e.printStackTrace();
            amqpTemplate.convertAndSend("ORDER-EXCHANGE","stock.unlock",orderSubmitVo.getOrderToken());
            throw new OrderException("订单保存错误");
        }

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("userId",userInfo.getUserId());
            List<Long> skuIds = items.stream().map(orderItemVo -> orderItemVo.getSkuId()).collect(Collectors.toList());
            map.put("skuIds",JSON.toJSONString(skuIds));
            amqpTemplate.convertAndSend("ORDER-EXCHANGE","cart.delete",map);
        } catch (AmqpException e) {
            e.printStackTrace();
        }

        return orderEntity;
    }
}

