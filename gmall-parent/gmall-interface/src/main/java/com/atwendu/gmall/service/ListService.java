package com.atwendu.gmall.service;

import com.atwendu.gmall.bean.SkuLsInfo;
import com.atwendu.gmall.bean.SkuLsParams;
import com.atwendu.gmall.bean.SkuLsResult;

public interface ListService {

    /**
     * 保存数据到es中
     * @param skuLsInfo
     */
    void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    /**
     * 检索数据
     * @param skuLsParams
     * @return
     */
    SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 记录每个商品被访问的次数
     * @param skuId
     */
    public void incrHotScore(String skuId);
}
