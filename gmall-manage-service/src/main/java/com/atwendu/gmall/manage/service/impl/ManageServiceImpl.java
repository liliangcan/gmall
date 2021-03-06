package com.atwendu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atwendu.gmall.bean.*;
import com.atwendu.gmall.config.RedisUtil;
import com.atwendu.gmall.manage.constant.ManageConst;
import com.atwendu.gmall.manage.mapper.*;
import com.atwendu.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import javax.persistence.Transient;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        //select * from baseAttrInfo where catalog3Id = ?
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        return baseAttrInfoMapper.select(baseAttrInfo);

        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);


    }

    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //????????????
        if(baseAttrInfo.getId() != null || baseAttrInfo.getId().length() > 0){
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else{
            //???????????? baseAttrInfo
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //baseAttrValue = ? ??????????????????????????????????????????
        //????????????????????? ??????attrId ?????????
        //delete from baseAttrValue where attrId = baseAttrInfo.getId();
        BaseAttrValue baseAttrValueDel = new BaseAttrValue();
        baseAttrValueDel.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueDel);


        //baseAttrValue
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if(attrValueList != null && attrValueList.size() > 0){
            //????????????
            for (BaseAttrValue baseAttrValue : attrValueList) {
                // private String id;
                // private String valueName;??????????????????
                // private String attrId; attrId=baseAttrInfo.getId();
                //????????????baseAttrInfo ???????????????????????????????????????????????????
//                baseAttrValue.setId("122");//????????????
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);

            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);

        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
        return baseAttrValueList;
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        //baseAttrInfo.id = baseAttrValue.getAttrId();
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        //???????????????????????????????????????????????????
        //select * from baseAttrValue where  attrId =?
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);

        baseAttrInfo.setAttrValueList(baseAttrValueList);
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        return null;
    }

    @Override
    public List<SpuInfo> getSpuList(SpuInfo spuInfo) {
        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);

        return spuInfoList;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transient
    public void saveSpuInfo(SpuInfo spuInfo) {
        //????????????
        //  spuInfo
        //  spuImage
        //  spuSaleAttr
        //  spuSaleAttrValue
        spuInfoMapper.insertSelective(spuInfo);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList != null && spuImageList.size() > 0) {
            for (SpuImage spuImage : spuImageList) {
                //??????spuId
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                //  spuSaleAttrValue
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if(spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {
        //select * from spuImage where spuId = spuImage.getSpuId();
        List<SpuImage> spuImageList = spuImageMapper.select(spuImage);
        return spuImageList;
    }

    @Override
    public List<SpuSaleAttr> getspuSaleAttrList(String spuId) {
        //??????mapper
        //???????????????????????????
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        // skuInfo
        skuInfoMapper.insertSelective(skuInfo);

        // skuImage
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList != null && skuImageList.size() > 0){
            for (SkuImage skuImage : skuImageList) {
                // skuImage.skuId = skuInfo.getId()
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }

        // skuAttrValue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList != null && skuAttrValueList.size() > 0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());

                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }

        // skuSaleAttrValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList != null && skuAttrValueList.size() > 0) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }

    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {

//        Jedis jedis = redisUtil.getJedis();
//        jedis.set("ok","?????????");
//        jedis.close();

        //Redisson??????
        return getSkuInfoRedisson(skuId);

        //jedis??????
//        return getSkuInfoJedis(skuId);




//        SkuInfo skuInfo = null;
//        Jedis jedis = null;
//        try {
//            jedis = redisUtil.getJedis();
//            //??????key,???????????????sku:skuId:info
//            String skuKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;
//            //???????????????????????????????????????????????????????????????????????????db??????????????????????????????
//            //??????redis????????????key
//            // ????????????????????????
//            if(jedis.exists(skuKey)) {
//                //??????key??????value
//                String skuJson = jedis.get(skuKey);
//                //???????????????????????????
//                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
////                jedis.close();
//               return skuInfo;
//            }else {
//                skuInfo = getSkuInfoDB(skuId);
//                //??????redis ?????????????????????
//                jedis.setex(skuKey, ManageConst.SKUKEY_TIMEOUT, JSON.toJSONString(skuInfo));
////                jedis.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if(jedis != null){
//                jedis.close();
//            }
//        }
//        return getSkuInfoDB(skuId);

    }

    private SkuInfo getSkuInfoRedisson(String skuId) {


//        try {
//            boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        //????????????????????????
        SkuInfo skuInfo = null;
        Jedis jedis = null;
        RLock lock = null;
        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://192.168.253.219:6379");

            RedissonClient redissonClient = Redisson.create(config);
            //??????Redisson??????getLock
            lock = redissonClient.getLock("yourLock");

            //??????
            lock.lock(10, TimeUnit.SECONDS);
            jedis = redisUtil.getJedis();
            //??????key,???????????????sku:skuId:info
            String skuKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;
            //???????????????????????????????????????????????????????????????????????????db??????????????????????????????
            //??????redis????????????key
            // ????????????????????????
            if(jedis.exists(skuKey)) {
                //??????key??????value
                String skuJson = jedis.get(skuKey);
                //???????????????????????????
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
//                jedis.close();
               return skuInfo;
            }else {
                skuInfo = getSkuInfoDB(skuId);
                //??????redis ?????????????????????
                jedis.setex(skuKey, ManageConst.SKUKEY_TIMEOUT, JSON.toJSONString(skuInfo));
//                jedis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis != null){
                jedis.close();
            }
            //??????
            if(lock != null) {
                lock.unlock();
            }

        }
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoJedis(String skuId) {
        //??????jedis
        SkuInfo skuInfo = null;
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            //??????key,???????????????sku:skuId:info
            String skuKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;
            //????????????
            String skuJson = jedis.get(skuKey);

            if(skuJson == null || skuJson.length() == 0) {
                //????????????
                System.out.println("?????????????????????");
                //??????set??????
                //???????????????key
                String skuLockKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKULOCK_SUFFIX;
                String lockKey = jedis.set(skuLockKey, "good", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if("OK".equals(lockKey)) {
                    //?????????????????????
                    skuInfo = getSkuInfoDB(skuId);
                    // ????????????????????????
                    // ???????????????????????????
                    String skuRedisStr = JSON.toJSONString(skuInfo);
                    jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT,skuRedisStr);
                    //????????????
                    jedis.del(skuLockKey);
                    return skuInfo;
                }else {
                    //??????
                    Thread.sleep(1000);

                    //??????getSkuInfo();
                    return getSkuInfo(skuId);
                }
            }else{
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis != null){
                jedis.close();
            }
        }
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
//        SkuImage skuImage = new SkuImage();
//        skuImage.setSkuId(skuId);
//        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
//        skuInfo.setSkuImageList(skuImageList);
        skuInfo.setSkuImageList(getSkuImageBySkuId(skuId));

        //???????????????????????????
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        skuInfo.setSkuAttrValueList(skuAttrValueMapper.select(skuAttrValue));
        return skuInfo;
    }

    @Override
    public List<SkuImage> getSkuImageBySkuId(String skuId) {
        //select * from skuImage where skuId = ?
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        return skuImageList;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        //????????????mapper ??????????????????
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        //??????spuId ????????????
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        //selec * from base_attr_info bai inner join base_attr_value bav on bai.id = bav.attr_id where bav.id in(80,82,83,13);
        //80,82,83,13???????????????????????????
        //????????????????????????
        String valueIds = StringUtils.join(attrValueIdList.toArray(), ",");
        System.out.println("valueIds:" + valueIds);
        return baseAttrInfoMapper.selectAttrInfoListByIds(valueIds);
    }
}
