package com.atwendu.gmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  //相当于.xml文件
public class RedisConfig {

    //disable 表示如果未从配置文件中获取host，则默认值为disable
    @Value("${spring.redis.host:disable}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    //将获取的数据传入到initJedisPool方法中
    @Bean   //相当于在xml中创建了一个<bean>标签
    public RedisUtil getRedisUtil() {
        if("disable".equals(host)) {
            return null;
        }

        RedisUtil redisUtil = new RedisUtil();
        //调用initJedisPool方法将值传入
        redisUtil.initJedisPool(host,port,database);

        return redisUtil;
    }
}
