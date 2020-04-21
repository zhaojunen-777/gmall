package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zje
 * @email zje@atguigu.com
 * @date 2020-01-15 16:51:12
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
