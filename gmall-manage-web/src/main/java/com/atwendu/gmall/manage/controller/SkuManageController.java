package com.atwendu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atwendu.gmall.bean.SkuInfo;
import com.atwendu.gmall.bean.SkuLsInfo;
import com.atwendu.gmall.bean.SpuImage;
import com.atwendu.gmall.bean.SpuSaleAttr;
import com.atwendu.gmall.service.ListService;
import com.atwendu.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

//    @RequestMapping("spuImageList")
//    public List<SpuImage> spuImageList(String spuId) {
//
//    }

    @RequestMapping("spuImageList")
    public List<SpuImage> spuImageList(SpuImage spuImage) {
        //调用service 层
        return manageService.getSpuImageList(spuImage);
    }

    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
        //调用service 层
        return manageService.getspuSaleAttrList(spuId);
    }

    @RequestMapping("saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        if(skuInfo != null){
            manageService.saveSkuInfo(skuInfo);
        }
    }
    //上传一个商品，如果上传批量
    @RequestMapping("onSale")
    public void onSale(String skuId){
        //创建一个skuLsInfo 对象
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        //给skuLsInfo 赋值
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //属性拷贝
        BeanUtils.copyProperties(skuInfo,skuLsInfo);
        listService.saveSkuLsInfo(skuLsInfo);

    }

}
