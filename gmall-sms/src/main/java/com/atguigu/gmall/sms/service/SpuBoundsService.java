package com.atguigu.gmall.sms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.sms.entity.SpuBoundsEntity;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * 商品spu积分设置
 *
 * @author zje
 * @email zje@atguigu.com
 * @date 2020-01-07 20:13:24
 */
public interface SpuBoundsService extends IService<SpuBoundsEntity> {

    PageVo queryPage(QueryCondition params);


}

