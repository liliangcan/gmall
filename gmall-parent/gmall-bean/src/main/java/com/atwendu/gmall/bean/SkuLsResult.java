package com.atwendu.gmall.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SkuLsResult implements Serializable {

    List<SkuLsInfo> skuLsInfoList;

    long total;

    long totalPages;

    //平台属性值Id集合
    List<String> attrValueIdList;

}

