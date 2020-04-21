package com.atguigu.gmall.sms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.sms.vo.SaleVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {
    @Autowired
    private SkuLadderDao ladderDao;

    @Autowired
    private SkuFullReductionDao reductionDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }
    @Transactional
    @Override
    public void saveSales(SaleVO saleVO) {
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(saleVO,skuBoundsEntity);
        List<String> works = saleVO.getWork();
        skuBoundsEntity.setWork(new Integer(works.get(0)) + new Integer(works.get(1)) * 2 + new Integer(works.get(2)) * 4 + new Integer(works.get(3)) * 8);
        this.save(skuBoundsEntity);

        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(saleVO,skuLadderEntity);
        skuLadderEntity.setAddOther(saleVO.getLadderAddOther());
        ladderDao.insert(skuLadderEntity);

        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(saleVO,reductionEntity);
        reductionEntity.setAddOther(saleVO.getFullAddOther());
        reductionDao.insert(reductionEntity);


    }

    @Override
    public List<ItemSaleVO> queryItemSaleVoBySkuId(Long skuId) {
        List<ItemSaleVO> itemSaleVOS = new ArrayList<>();
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (skuBoundsEntity != null){
            ItemSaleVO itemSaleVO = new ItemSaleVO();
            itemSaleVO.setType("积分");
            itemSaleVO.setDesc("赠送"+skuBoundsEntity.getGrowBounds()+"成长积分，"+skuBoundsEntity.getBuyBounds()+"购物积分");
            itemSaleVOS.add(itemSaleVO);
        }

        SkuLadderEntity skuLadderEntity = ladderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (skuLadderEntity != null){
            ItemSaleVO itemSaleVO = new ItemSaleVO();
            itemSaleVO.setType("满减");
            itemSaleVO.setDesc("满"+skuLadderEntity.getFullCount()+"打"+skuLadderEntity.getDiscount().divide(new BigDecimal(10))+"折");
            itemSaleVOS.add(itemSaleVO);
        }

        SkuFullReductionEntity reductionEntity = reductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (reductionEntity != null){
            ItemSaleVO itemSaleVO = new ItemSaleVO();
            itemSaleVO.setType("满减");
            itemSaleVO.setDesc("满"+reductionEntity.getFullPrice()+"减"+reductionEntity.getReducePrice());
            itemSaleVOS.add(itemSaleVO);
        }

        return itemSaleVOS;
    }

}