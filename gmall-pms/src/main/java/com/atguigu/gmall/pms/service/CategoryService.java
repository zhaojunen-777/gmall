package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.CategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品三级分类
 *
 * @author zje
 * @email zje@atguigu.com
 * @date 2020-01-02 16:30:31
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageVo queryPage(QueryCondition params);

    List<CategoryEntity> queryCategoriesByLevelOrPid(Integer level, Long pid);

    List<CategoryVO> queryCategoriesWithSub(Long pid);
}

