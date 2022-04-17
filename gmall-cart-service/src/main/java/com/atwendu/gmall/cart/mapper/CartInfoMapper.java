package com.atwendu.gmall.cart.mapper;

import com.atwendu.gmall.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {

    /**
     * 根据用户Id查询实时价格 到cartInfo中
     * @param userId
     * @return
     */
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
