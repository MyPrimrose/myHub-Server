package com.myhub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.myhub")
public class MyHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyHubApplication.class, args);
    }
}
