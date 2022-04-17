package com.atwendu.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atwendu.gmall.bean.SpuInfo;
import com.atwendu.gmall.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class SpuManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> spuList(String catalog3Id){
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfoList = manageService.getSpuList(spuInfo);
        return  spuInfoList;
    }

//    public List<SpuInfo> spuList(SpuInfo spuInfo) {
//        return manageService.getSpuList(spuInfo);
//    }

    @RequestMapping("saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){
        if(spuInfo != null){
            //调用保存
            manageService.saveSpuInfo(spuInfo);
        }

    }

}
