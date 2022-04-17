package com.atwendu.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atwendu.gmall.bean.*;
import com.atwendu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class ManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
        return manageService.getCatalog1();
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id){

        return manageService.getCatalog2(catalog1Id);
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id){

        return manageService.getCatalog3(catalog2Id);
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){

        return manageService.getAttrList(catalog3Id);
    }


    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        //传递的什么
        manageService.saveAttrInfo(baseAttrInfo);
    }

//    @RequestMapping("getAttrValueList")
//    @ResponseBody
//    public List<BaseAttrValue> getAttrValueList(String attrId){
//        //select * from baseAttrValue where  attrId =?
//        return manageService.getAttrValueList(attrId);
//    }

    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        //先通过attrId 查询平台属性  select * from baseAttrInfo where id = attrId
        BaseAttrInfo baseAttrInfo = manageService.getAttrInfo(attrId);
        //返回平台属性中的平台属性值集合 baseAttrInfo.getAttrValueList()
        return baseAttrInfo.getAttrValueList();
    }


    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){
        //传递的什么
        return manageService.getBaseSaleAttrList();
    }

//    @RequestMapping("spuSaleAttrList")
//    @ResponseBody
//    public List<SpuSaleAttr> spuSaleAttrList(SpuInfo spuInfo) {
//        return manageService.getSpuSaleAttrList(spuInfo);
//    }
}
