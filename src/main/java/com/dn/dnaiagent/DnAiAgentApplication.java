package com.dn.dnaiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dn.dnaiagent.mapper")
public class DnAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DnAiAgentApplication.class, args);
    }

}
