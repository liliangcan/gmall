package com.atwendu.gmall.passport;

import com.atwendu.gmall.passport.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GmallPassportWebApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void testJWT() {
        //出现过java.lang.NoClassDefFoundError: javax/xml/bind/DatatypeConverter 报错
        //原因分析：
        //JAXB API是java EE 的API，因此在java SE 9.0 中不再包含这个 Jar 包。
        //java9 中引入了模块的概念，默认情况下，Java SE中将不再包含java EE 的Jar包。而在 java 6/7/8 时关于这个API 都是捆绑在一起
        //解决办法：
        //方式一：降低JDK 版本到 JDK 8
        //方式二：手动添加以下依赖Jar包
        //<dependency>
        //    <groupId>javax.xml.bind</groupId>
        //    <artifactId>jaxb-api</artifactId>
        //    <version>2.3.0</version>
        //</dependency>
        //问题解决

        String key = "atwendu";
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", 1001);
        map.put("nickName","admin");
        String salt = "192.168.253.123";
        String token = JwtUtil.encode(key, map, salt);

        System.out.println("token:" + token);
        //token:eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6ImFkbWluIiwidXNlcklkIjoxMDAxfQ.PXgEvqckTcRHgXkn3WKw4Z2qR1gzC2qnVaOMa0j18aw

        //解密token
        Map<String, Object> maps = JwtUtil.decode(token, key, salt);
        System.out.println(maps);


    }
}
