package com.atguigu.gmall.item.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemService {

    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public ItemVo queryItemVo(Long skuId) {

        ItemVo itemVo = new ItemVo();
        itemVo.setSkuId(skuId);
        CompletableFuture<SkuInfoEntity> skuInfoEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = gmallPmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity == null) {
                return null;
            }
            itemVo.setWeight(skuInfoEntity.getWeight());
            itemVo.setSkuTitle(skuInfoEntity.getSkuTitle());
            itemVo.setSkuSubtitle(skuInfoEntity.getSkuSubtitle());
            itemVo.setPrice(skuInfoEntity.getPrice());
            return skuInfoEntity;
        },threadPoolExecutor);
        CompletableFuture<Void> categoryvoidCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<CategoryEntity> categoryEntityResp = gmallPmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
            CategoryEntity categoryEntity = categoryEntityResp.getData();
            if (categoryEntity != null) {
                itemVo.setCatalogId(categoryEntity.getCatId());
                itemVo.setCategoryName(categoryEntity.getName());
            }
        },threadPoolExecutor);

        CompletableFuture<Void> brandCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<BrandEntity> brandEntityResp = gmallPmsClient.queryBrandById(skuInfoEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResp.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getBrandId());
                itemVo.setBrandName(brandEntity.getName());
            }
        },threadPoolExecutor);

        CompletableFuture<Void> spuInfoCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<SpuInfoEntity> spuInfoEntityResp = gmallPmsClient.querySpuById(skuInfoEntity.getSpuId());
            SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
            if (spuInfoEntity != null) {
                itemVo.setSpuId(spuInfoEntity.getId());
                itemVo.setSpuName(spuInfoEntity.getSpuName());
            }
        },threadPoolExecutor);

        CompletableFuture<Void> skuImagesCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<SkuImagesEntity>> listResp = gmallPmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = listResp.getData();
            itemVo.setImages(skuImagesEntities);
        },threadPoolExecutor);

        CompletableFuture<Void> wareSkuCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<WareSkuEntity>> listResp1 = gmallWmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = listResp1.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }
        },threadPoolExecutor);

        CompletableFuture<Void> itemSaleVOCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<ItemSaleVO>> itemSaleVoResp = gmallSmsClient.queryItemSaleVoBySkuId(skuId);
            List<ItemSaleVO> itemSaleVOList = itemSaleVoResp.getData();
            itemVo.setSales(itemSaleVOList);
        },threadPoolExecutor);

        CompletableFuture<Void> spuInfoDescCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = gmallPmsClient.querySpuDescBySpuId(skuInfoEntity.getSpuId());
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescEntityResp.getData();
            if (spuInfoDescEntity != null && StringUtils.isNotBlank(spuInfoDescEntity.getDecript())) {
                itemVo.setDesc(Arrays.asList(StringUtils.split(spuInfoDescEntity.getDecript(), ",")));
            }
        },threadPoolExecutor);

        CompletableFuture<Void> itemGroupVOSCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<List<ItemGroupVO>> groupResp = gmallPmsClient.queryItemGroupByCidAndSpuId(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId());
            List<ItemGroupVO> itemGroupVOS = groupResp.getData();
            itemVo.setGroupVOS(itemGroupVOS);
        },threadPoolExecutor);

        CompletableFuture<Void> saleAttrValueCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<List<SkuSaleAttrValueEntity>> skuAttrValueResp = gmallPmsClient.querySaleAttrValueBySpuId(skuInfoEntity.getSpuId());
            List<SkuSaleAttrValueEntity> saleAttrValueEntities = skuAttrValueResp.getData();
            itemVo.setSaleAttrValues(saleAttrValueEntities);
        },threadPoolExecutor);

        CompletableFuture.allOf(categoryvoidCompletableFuture,
                brandCompletableFuture,
                spuInfoCompletableFuture,
                skuImagesCompletableFuture,
                wareSkuCompletableFuture,
                itemSaleVOCompletableFuture,
                spuInfoDescCompletableFuture,
                itemGroupVOSCompletableFuture,
                saleAttrValueCompletableFuture).join();
        return itemVo;

    }
}
