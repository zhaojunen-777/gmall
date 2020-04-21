package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

public class StockListener {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private WareSkuDao wareSkuDao;

    private static final String KEY_PREFIX = "wms:stock";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "STOCK-UNLOCK-QUEUE", durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock.unlock", "wms.dead"}
    ))
    public void unlock(String orderToken){

        String json = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        if (StringUtils.isEmpty(json)) {
            return ;
        }
        // 反序列化锁定库存信息
        List<SkuLockVo> skuLockVOS = JSON.parseArray(json, SkuLockVo.class);
        skuLockVOS.forEach(skuLockVO -> {
            this.wareSkuDao.unLock(skuLockVO.getWareSkuId(), skuLockVO.getCount());
            this.stringRedisTemplate.delete(KEY_PREFIX + orderToken);
        });
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "STOCK-MINUS-QUEUE", durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock.minus"}
    ))
    public void minus(String orderToken){

        String json = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        if (StringUtils.isEmpty(json)) {
            return ;
        }
        // 反序列化锁定库存信息
        List<SkuLockVo> skuLockVOS = JSON.parseArray(json, SkuLockVo.class);
        skuLockVOS.forEach(skuLockVO -> {
            this.wareSkuDao.minus(skuLockVO.getWareSkuId(), skuLockVO.getCount());
            this.stringRedisTemplate.delete(KEY_PREFIX + orderToken);
        });

    }

//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "STOCK-UNLOCK-QUEUE",durable = "true"),
//            exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
//            key = {"stock.unlock","wms.dead"}
//    ))
//    public void unlock(String orderToken){
//
//        String json = stringRedisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
//        List<SkuLockVo> skuLockVos = JSON.parseArray(json, SkuLockVo.class);
//        skuLockVos.forEach(skuLockVo -> {
//            wareSkuDao.unLock(skuLockVo.getSkuId(),skuLockVo.getCount());
//        });
//
//    }
}
