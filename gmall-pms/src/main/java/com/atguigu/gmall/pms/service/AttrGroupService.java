package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性分组
 *
 * @author zje
 * @email zje@atguigu.com
 * @date 2020-01-02 16:30:32
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryGroupsByCidPage(QueryCondition queryCondition, Long catId);

    GroupVO queryGroupVOByGid(Long gid);

    List<GroupVO> queryGroupVOsByCid(Long catId);

    List<ItemGroupVO> queryItemGroupByCidAndSpuId(Long cid, Long spuId);
}

