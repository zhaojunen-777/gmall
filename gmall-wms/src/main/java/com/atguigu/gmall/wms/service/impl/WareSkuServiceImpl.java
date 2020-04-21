package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String KEY_PREFIX = "wms:stock";

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> skuLockVos) {

        if (CollectionUtils.isEmpty(skuLockVos)){
            return null;
        }

        skuLockVos.forEach(skuLockVo -> {
            this.checkLock(skuLockVo);
        });

        if (skuLockVos.stream().anyMatch(skuLockVo -> skuLockVo.getLock() == false)){
            skuLockVos.stream().filter(skuLockVo -> skuLockVo.getLock()).forEach(skuLockVo -> {
                wareSkuDao.unLock(skuLockVo.getWareSkuId(), skuLockVo.getCount());
            });
            return skuLockVos;
        }
        String orderToken = skuLockVos.get(0).getOrderToken();
        stringRedisTemplate.opsForValue().set(KEY_PREFIX+orderToken, JSON.toJSONString(skuLockVos));

        amqpTemplate.convertAndSend("ORDER-EXCHANGE","wms.ttl",orderToken);
        return null;
    }

    private void checkLock(SkuLockVo skuLockVo){

        RLock lock = redissonClient.getFairLock("lock" + skuLockVo.getSkuId());
        lock.lock();
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.check(skuLockVo.getSkuId(), skuLockVo.getCount());
        if (!CollectionUtils.isEmpty(wareSkuEntities)){
            WareSkuEntity wareSkuEntity = wareSkuEntities.get(0);
            int lock1 = wareSkuDao.lock(wareSkuEntity.getId(), skuLockVo.getCount());
            if (lock1 != 0){
                skuLockVo.setLock(true);
                skuLockVo.setWareSkuId(wareSkuEntity.getId());
            }
        }
        lock.unlock();
    }


}