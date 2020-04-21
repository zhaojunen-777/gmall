package com.atguigu.gmall.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.oms.entity.RefundInfoEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 退款信息
 *
 * @author zje
 * @email zje@atguigu.com
 * @date 2020-01-14 21:18:19
 */
public interface RefundInfoService extends IService<RefundInfoEntity> {

    PageVo queryPage(QueryCondition params);
}

