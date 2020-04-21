package com.atguigu.gmall.wms.service;

import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品库存
 *
 * @author zje
 * @email zje@atguigu.com
 * @date 2020-01-02 18:28:12
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageVo queryPage(QueryCondition params);

    List<SkuLockVo> checkAndLock(List<SkuLockVo> skuLockVos);
}

