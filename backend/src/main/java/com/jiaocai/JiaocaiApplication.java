package com.jiaocai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.jiaocai.mapper")
public class JiaocaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JiaocaiApplication.class, args);
    }

}
