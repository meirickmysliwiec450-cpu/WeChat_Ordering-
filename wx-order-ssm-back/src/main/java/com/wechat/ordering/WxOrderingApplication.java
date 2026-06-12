package com.wechat.ordering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

/**
 * 微信点餐系统 - 主应用程序
 */
@SpringBootApplication
@MapperScan("com.wechat.ordering.mapper")
public class WxOrderingApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxOrderingApplication.class, args);
        System.out.println("微信点餐系统后端启动成功！");
    }
}
