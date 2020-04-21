package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "cart:item:";

    private static final String PRICE_PREFIX = "cart:price";

    public void addCart(Cart cart) {

        String key = KEY_PREFIX;
        UserInfo userInfo = LoginInterceptor.userInfo();
        if (userInfo.getUserId() != null){
            key+= userInfo.getUserId();
        }else {
            key+=userInfo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(key);

        String skuId = cart.getSkuId().toString();
        Integer count = cart.getCount();

        if (hashOps.hasKey(skuId)){
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount()+count);
        }else {
            cart.setCheck(true);

            Resp<SkuInfoEntity> skuInfoEntityResp = pmsClient.querySkuById(cart.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity == null){
                return;
            }

            cart.setPrice(skuInfoEntity.getPrice());
            cart.setImage(skuInfoEntity.getSkuDefaultImg());
            cart.setSkuTitle(skuInfoEntity.getSkuTitle());

            Resp<List<WareSkuEntity>> wareSkuResp = wmsClient.queryWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
            cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()>0));

            Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = pmsClient.querySaleAttrValueBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = saleAttrValueResp.getData();
            cart.setSaleAttrs(skuSaleAttrValueEntities);

            Resp<List<ItemSaleVO>> listResp = smsClient.queryItemSaleVoBySkuId(cart.getSkuId());
            List<ItemSaleVO> itemSaleVOS = listResp.getData();
            cart.setSales(itemSaleVOS);

            stringRedisTemplate.opsForValue().set(PRICE_PREFIX+skuId,skuInfoEntity.getPrice().toString());
        }

        hashOps.put(skuId,JSON.toJSONString(cart));
    }

    public List<Cart> queryCarts() {

        UserInfo userInfo = LoginInterceptor.userInfo();
        String userKey = KEY_PREFIX + userInfo.getUserKey();
        Long userId = userInfo.getUserId();

        BoundHashOperations<String, Object, Object> userKeyHashOps = stringRedisTemplate.boundHashOps(userKey);
        List<Object> values = userKeyHashOps.values();
        List<Cart> userKeyCarts = null;
        if (!CollectionUtils.isEmpty(values)){
            userKeyCarts = values.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                String currentPrice = stringRedisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(currentPrice));
                return cart;
            }).collect(Collectors.toList());
        }

        if (userId == null){
            return userKeyCarts;
        }

        String userIdKey = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> userIdKeyhashOps = stringRedisTemplate.boundHashOps(userIdKey);
        if (!CollectionUtils.isEmpty(userKeyCarts)){
            userKeyCarts.forEach(cart -> {
                if (userIdKeyhashOps.hasKey(cart.getSkuId().toString())){
                    String cartJson = userIdKeyhashOps.get(cart.getSkuId().toString()).toString();
                    Integer count = cart.getCount();
                    cart = JSON.parseObject(cartJson,Cart.class);
                    cart.setCount(cart.getCount()+count);
                }
                userIdKeyhashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
            });
            stringRedisTemplate.delete(userKey);
        }

        List<Object> userIdVales = userIdKeyhashOps.values();
        if (!CollectionUtils.isEmpty(userIdVales)){
            return userIdVales.stream().map(userIdVale -> {
                Cart cart = JSON.parseObject(userIdVale.toString(), Cart.class);
                String currentPrice = stringRedisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(currentPrice));
                return cart;
            }).collect(Collectors.toList());
        }

        return null;
    }

    public void updateNum(Cart cart) {

        UserInfo userInfo = LoginInterceptor.userInfo();
        String key = KEY_PREFIX;
        if (userInfo.getUserId() != null){
            key+=userInfo.getUserId();
        }else {
            key+=userInfo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(key);
        if (hashOps.hasKey(cart.getSkuId().toString())){
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            Integer count = cart.getCount();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            hashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
        }
    }

    public void check(Cart cart) {

        UserInfo userInfo = LoginInterceptor.userInfo();
        String key = KEY_PREFIX;
        if (userInfo.getUserId() != null){
            key+=userInfo.getUserId();
        }else {
            key+=userInfo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(key);
        if (hashOps.hasKey(cart.getSkuId().toString())){
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            Boolean check = cart.getCheck();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCheck(check);
            hashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
        }
    }

    public void delete(Long skuId) {

        UserInfo userInfo = LoginInterceptor.userInfo();
        String key = KEY_PREFIX;
        if (userInfo.getUserId() != null){
            key+=userInfo.getUserId();
        }else {
            key+=userInfo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())){
            hashOps.delete(skuId.toString());
        }
    }

    public List<Cart> queryCheckedCarts(Long userId) {

        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> values = hashOps.values();
        if (!CollectionUtils.isEmpty(values)){
            return values.stream().map(cartJson -> JSON.parseObject(cartJson.toString(),Cart.class)).filter(cart -> cart.getCheck()).collect(Collectors.toList());
        }
        return null;
    }
}
