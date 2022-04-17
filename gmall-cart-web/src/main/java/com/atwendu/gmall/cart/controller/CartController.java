package com.atwendu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atwendu.gmall.bean.CartInfo;
import com.atwendu.gmall.bean.SkuInfo;
import com.atwendu.gmall.config.LoginRequire;
import com.atwendu.gmall.service.CartService;
import com.atwendu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    @Reference
    private ManageService manageService;

    //如何区分用户是否登录？ 只需要看userId
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response) {

        //获取商品数量
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");

        //获取useId
        String userId = (String) request.getAttribute("userId");
        if(userId != null) {
            //调用登录添加购物车
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else{
            //调用未登录添加购物车
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }
        //根据skuId 查询skuInfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
//
        request.setAttribute("skuNum",skuNum);
        request.setAttribute("skuInfo",skuInfo);
        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response) {

        //获取useId
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfoList = null;

        if(userId != null) {
            //合并购物车
            List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);
            if(cartListCK != null && cartListCK.size() > 0) {
                //合并购物车
                cartInfoList = cartService.mergeToCartList(cartListCK,userId);
                //删除未登录购物车
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                //登录状态下查询购物车
                cartInfoList = cartService.getCartList(userId);
            }
        }else{
            //未登录状态下查询购物车
            cartInfoList = cartCookieHandler.getCartList(request);
        }
        //保存购物车集合
        request.setAttribute("cartInfoList",cartInfoList);

        return "cartList";
    }

    @RequestMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(HttpServletRequest request,HttpServletResponse response) {
        //获取页面传递过来的数据
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");

        String userId = (String) request.getAttribute("userId");

        if(userId != null) {
            //登录状态
            cartService.checkCart(skuId,isChecked,userId);
        }else{
            //未登录状态
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request,HttpServletResponse response) {
        //合并勾选的商品 未登录+登录的
        List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);

        String userId = (String) request.getAttribute("userId");
        if(cartListCK != null && cartListCK.size() > 0){
            //合并
            cartService.mergeToCartList(cartListCK,userId);
            //删除未登录数据
            cartCookieHandler.deleteCartCookie(request,response);
        }


        return "redirect://order.gmall.com/trade";
    }




}
