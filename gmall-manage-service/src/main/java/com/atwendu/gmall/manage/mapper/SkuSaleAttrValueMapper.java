package com.atwendu.gmall.manage.mapper;

import com.atwendu.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    //根据spuId 查询数据
    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);
}
