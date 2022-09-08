package com.suplin.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.suplin.demo.mapper")
@SpringBootApplication
// 定时器
@EnableScheduling
// 异步化
@EnableAsync
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(DemoApplication.class);
        ConfigurableEnvironment environment = springApplication.run(args).getEnvironment();
        Logger logger = LoggerFactory.getLogger(DemoApplication.class);
        logger.info("启动成功");
        logger.info("http://localhost:" + environment.getProperty("server.port"));

    }
}
