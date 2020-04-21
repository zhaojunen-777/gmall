package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private ProductAttrValueDao attrValueDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryGroupsByCidPage(QueryCondition queryCondition, Long catId) {
        IPage<AttrGroupEntity> page = page(new Query<AttrGroupEntity>().getPage(queryCondition),
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id",catId));
        return new PageVo(page);
    }

    @Override
    public GroupVO queryGroupVOByGid(Long gid) {
        GroupVO groupVO = new GroupVO();
        AttrGroupEntity attrGroupEntity = this.getById(gid);
        BeanUtils.copyProperties(attrGroupEntity,groupVO);
        List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id",gid));
        groupVO.setRelations(relationEntities);
        if (CollectionUtils.isEmpty(relationEntities)) {
            return groupVO;
        }
        List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        List<AttrEntity> attrEntities = attrDao.selectBatchIds(attrIds);
        groupVO.setAttrEntities(attrEntities);
        return groupVO;
    }

    @Override
    public List<GroupVO> queryGroupVOsByCid(Long catId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id",catId));
        return attrGroupEntities.stream().map(attrGroupEntity -> this.queryGroupVOByGid(attrGroupEntity.getAttrGroupId())).collect(Collectors.toList());

    }

    @Override
    public List<ItemGroupVO> queryItemGroupByCidAndSpuId(Long cid, Long spuId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        if (CollectionUtils.isEmpty(attrGroupEntities)){
            return null;
        }
        return attrGroupEntities.stream().map(group-> {
            ItemGroupVO itemGroupVO = new ItemGroupVO();
            itemGroupVO.setId(group.getAttrGroupId());
            itemGroupVO.setName(group.getAttrGroupName());
            List<AttrAttrgroupRelationEntity> attrgroupRelationEntities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", group.getAttrGroupId()));
            if (!CollectionUtils.isEmpty(attrgroupRelationEntities)){
                List<Long> attrIds = attrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
                List<ProductAttrValueEntity> attrValueEntities = attrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).in("attr_id", attrIds));
                itemGroupVO.setAttrs(attrValueEntities);
            }

            return itemGroupVO;

        }).collect(Collectors.toList());
    }

}