package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.dao.OrderItemDao;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.GmallPmsClient;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private OrderItemDao itemDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public OrderEntity saveOrder(OrderSubmitVo orderSubmitVo, Long userId) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSubmitVo.getOrderToken());
        orderEntity.setTotalAmount(orderSubmitVo.getTotalPrice());
        orderEntity.setPayType(orderSubmitVo.getPayType());
        orderEntity.setSourceType(0);
        orderEntity.setDeliveryCompany(orderSubmitVo.getDeliveryCompany());
        orderEntity.setCreateTime(new Date());
        orderEntity.setModifyTime(orderEntity.getCreateTime());
        orderEntity.setStatus(0);
        orderEntity.setDeleteStatus(0);
        orderEntity.setMemberId(userId);

        MemberReceiveAddressEntity address = orderSubmitVo.getAddress();
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverRegion(address.getRegion());

        boolean b = this.save(orderEntity);
        if (b){
            List<OrderItemVo> items = orderSubmitVo.getItems();
            if (!CollectionUtils.isEmpty(items)){
                items.forEach(orderItemVo -> {
                    OrderItemEntity itemEntity = new OrderItemEntity();
                    itemEntity.setOrderSn(orderSubmitVo.getOrderToken());
                    itemEntity.setOrderId(orderEntity.getId());
                    Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.querySkuById(orderItemVo.getSkuId());
                    SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                    if (skuInfoEntity != null){
                        itemEntity.setSkuId(orderItemVo.getSkuId());
                        itemEntity.setSkuQuantity(orderItemVo.getCount());
                        itemEntity.setSkuPic(orderItemVo.getImage());
                        itemEntity.setSkuName(orderItemVo.getSkuTitle());
                        itemEntity.setSkuAttrsVals(JSON.toJSONString(orderItemVo.getSaleAttrs()));
                        itemEntity.setSkuPrice(skuInfoEntity.getPrice());

                        Long spuId = skuInfoEntity.getSpuId();
                        Resp<SpuInfoEntity> spuInfoEntityResp = this.pmsClient.querySpuById(spuId);
                        SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
                        if (spuInfoEntity != null){
                            itemEntity.setSpuId(spuId);
                            itemEntity.setSpuName(spuInfoEntity.getSpuName());
                            itemEntity.setSpuBrand(spuInfoEntity.getBrandId().toString());
                            itemEntity.setCategoryId(spuInfoEntity.getCatalogId());
                        }
                    }

                    itemDao.insert(itemEntity);
                });
            }
        }

        return orderEntity;
    }

}