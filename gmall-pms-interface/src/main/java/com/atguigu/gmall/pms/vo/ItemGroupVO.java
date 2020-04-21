package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;

import java.util.List;

@Data
public class ItemGroupVO {

    private Long id;
    private String name;//分组的名字
    private List<ProductAttrValueEntity> attrs;
}
