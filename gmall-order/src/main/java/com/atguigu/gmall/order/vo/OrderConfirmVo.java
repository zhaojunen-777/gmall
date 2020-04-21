package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVo {

    private List<MemberReceiveAddressEntity> addressEntities;
    private List<OrderItemVo> orderItemVos;
    private Integer bounds;
    private String orderToken;
}
