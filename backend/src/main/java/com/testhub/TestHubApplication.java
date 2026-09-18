package com.testhub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TestHub 企业级智能测试平台启动类
 */
@SpringBootApplication
@MapperScan("com.testhub.mapper")
@EnableAsync
@EnableScheduling
public class TestHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestHubApplication.class, args);
        System.out.println("""

                ╔══════════════════════════════════════════════╗
                ║        TestHub 智能测试平台启动成功           ║
                ║        API文档: http://localhost:8080/api/doc.html
                ╚══════════════════════════════════════════════╝
                """);
    }
}
