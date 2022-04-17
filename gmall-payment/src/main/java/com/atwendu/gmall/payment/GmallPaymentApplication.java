package com.atwendu.gmall.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atwendu.gmall.payment.mapper")
@ComponentScan(basePackages = "com.atwendu.gmall")
public class GmallPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPaymentApplication.class, args);
    }

}
