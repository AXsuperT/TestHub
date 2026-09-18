package com.testhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TestHub 企业级智能测试平台 API")
                        .version("1.0.0")
                        .description("TestHub 是一套企业级智能测试平台，提供测试用例管理、接口自动化测试、UI自动化测试、性能测试、缺陷管理及AI智能助手等能力。")
                        .contact(new Contact().name("TestHub Team").email("admin@testhub.com")));
    }
}
