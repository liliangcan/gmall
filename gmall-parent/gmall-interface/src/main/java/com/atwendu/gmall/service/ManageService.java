package com.atwendu.gmall.service;

import com.atwendu.gmall.bean.*;

import java.util.List;

public interface ManageService {

    /**
     * 获取所有的一级分类数据
     * @return
     */
    List<BaseCatalog1> getCatalog1();

    /**
     * 根据一级分类Id查询二级分类数据
     * select * from baseCatalog2 where catalog1Id = ?
     * @param catalog1Id
     * @return
     */
    List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 根据二级分类Id查询三级分类数据
     * @param catalog2Id
     * @return
     */
    List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 根据三级分类Id查询平台属性集合
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getAttrList(String catalog3Id);


    /**
     * 保存平台属性数据
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据平台属性Id查询平台属性值集合
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(String attrId);

    /**
     * 根据平台属性Id 查询平台属性对象
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(String attrId);

    List<SpuInfo> getSpuList(String catalog3Id);

    /**
     * 根据spuInfo对象属性获取spuInfo集合
     * @param spuInfo
     * @return
     */
    List<SpuInfo> getSpuList(SpuInfo spuInfo);

    /**
     * 获取所有的销售属性
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存spuInfo
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 获取图片列表
     * @param spuImage
     * @return
     */
    List<SpuImage> getSpuImageList(SpuImage spuImage);

    /**
     * 根据spuId 获取销售属性集合
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getspuSaleAttrList(String spuId);

    /**
     * 保存skuInfo 数据
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 根据skuId 查询skuInfo
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(String skuId);

    /**
     * 根据skuId 查询skuImage 集合
     * @param skuId
     * @return
     */
    List<SkuImage> getSkuImageBySkuId(String skuId);

    /**
     * 根据skuId, spuId 查询销售属性集合
     * @param skuInfo
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     * 根据spuId 查询销售属性值集合
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    /**
     * 根据平台属性值Id查询
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
