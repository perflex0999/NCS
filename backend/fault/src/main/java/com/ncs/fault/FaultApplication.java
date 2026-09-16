package com.ncs.fault;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.ncs.fault.mapper")
@EnableDiscoveryClient
public class FaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(FaultApplication.class, args);
    }
}
