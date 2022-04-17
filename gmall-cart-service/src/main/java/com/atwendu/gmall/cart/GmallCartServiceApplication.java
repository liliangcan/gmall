package com.atwendu.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atwendu.gmall")
@MapperScan(basePackages = "com.atwendu.gmall.cart.mapper")
public class GmallCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallCartServiceApplication.class, args);
    }

}
