package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author zje
 * @email zje@atguigu.com
 * @date 2020-01-14 21:18:20
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    public int payOrder(String orderToken);
	
}
