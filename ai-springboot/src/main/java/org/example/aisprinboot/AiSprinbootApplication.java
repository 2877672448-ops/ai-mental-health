package org.example.aisprinboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.aisprinboot.mapper")
public class AiSprinbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiSprinbootApplication.class, args);
    }

}
