package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author zje
 * @email zje@atguigu.com
 * @date 2020-01-02 18:28:12
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    public List<WareSkuEntity> check(@Param("skuId") Long skuId, @Param("count") Integer count);

    public int lock(@Param("wareSkuId") Long wareSkuId,@Param("count") Integer count);

    public int unLock(@Param("wareSkuId") Long wareSkuId,@Param("count") Integer count);

    public int minus(@Param("wareSkuId") Long wareSkuId,@Param("count") Integer count);
	
}
