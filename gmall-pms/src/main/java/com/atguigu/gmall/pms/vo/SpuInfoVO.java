package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;
@Data
public class SpuInfoVO extends SpuInfoEntity {
    private List<String> spuImages;
    private List<BaseAttrValueVO> baseAttrs;
    private List<SkuInfoVO> skus;

}
