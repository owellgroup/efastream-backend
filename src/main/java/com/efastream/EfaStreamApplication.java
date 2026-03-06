package com.efastream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EfaStreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(EfaStreamApplication.class, args);
    }
}
