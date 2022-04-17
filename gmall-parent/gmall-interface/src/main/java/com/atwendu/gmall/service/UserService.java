package com.atwendu.gmall.service;

import com.atwendu.gmall.bean.UserAddress;
import com.atwendu.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {
    /**
     * 查询所有数据
     * @return
     */
    List<UserInfo> findAll();

    /**
     * 根据userId查询用户地址列表
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressList(String userId);

    /**
     * 登录方法
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 根据用户Id查询数据
     * @param userId
     * @return
     */
    UserInfo verify(Object userId);
}
