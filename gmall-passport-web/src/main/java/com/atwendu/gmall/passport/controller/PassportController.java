package com.atwendu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atwendu.gmall.bean.UserInfo;
import com.atwendu.gmall.passport.config.JwtUtil;
import com.atwendu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Value("${token.key}")
    private String key;

    //调用服务层
    @Reference
    private UserService userService;


    @RequestMapping("index")
    public String index(HttpServletRequest request) {
        //获取originUrl
        //http://localhost:8087/index?originUrl=https%3A%2F%2Fwww.jd.com%2F
        //http://passport.atwendu.com/index?originUrl=https%3A%2F%2Fwww.jd.com%2F
        String originUrl = request.getParameter("originUrl");

        //此处有问题，获取不到这个参数
        //找到原因了，初始网址加上后一段之后要先刷新再填登录信息，不然保存的网址还是最初的那个

//        String path = request.getRequestURL().toString();
//        System.out.println(path);

//        if(originUrl != null){
//            System.out.println(originUrl);
//        }else {
//            System.out.println(0);
//        }

        //保存originUrl
        request.setAttribute("originUrl", originUrl);
        return "index";
    }

    //控制器获取页面的数据
    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request) {
        //salt 服务器的IP地址
        String salt = request.getHeader("X-forwarded-for");
        //调用登录方法
        UserInfo info = userService.login(userInfo);
        if(info != null) {
            //如果登录成功之后，返回token
            //如何制作token
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId", info.getId());
            map.put("nickName",info.getNickName());
            //生成token
            String token = JwtUtil.encode(key, map, salt);
            return token;
            //https://www.jd.com/?newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.Hdq8pn4ilt_LSZAhsB1fxeNFJ6L6jAL1wifyLxJHMCo
        }else{
            return "fail";
        }
    }

    //http://passport.atwendu.com/verify?token=xxx&salt=xxx
    //http://passport.atwendu.com/verify?token=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.Hdq8pn4ilt_LSZAhsB1fxeNFJ6L6jAL1wifyLxJHMCo&salt=192.168.253.1
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request) {
        //获取服务器Ip token
//        String salt = request.getHeader("X-forwarded-for");
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");
        //调用jwt工具类
        Map<String, Object> map = JwtUtil.decode(token, key, salt);

        if(map != null && map.size() > 0) {
            //获取userId
            String userId = (String)map.get("userId");
            UserInfo userInfo = userService.verify(userId);
            if(userInfo != null) {
                return "success";
            }else{
                return "fail";
            }
        }
        return "fail";
    }

}
