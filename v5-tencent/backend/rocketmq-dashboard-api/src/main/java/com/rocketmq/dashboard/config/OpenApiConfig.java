package com.rocketmq.dashboard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RocketMQ Dashboard API")
                        .version("1.0.0")
                        .description("Modern dashboard for monitoring and managing Apache RocketMQ with Tencent Cloud")
                        .contact(new Contact()
                                .name("RocketMQ Dashboard Team")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.example.com").description("Production Server")
                ));
    }

    @Bean
    public GroupedOpenApi configApi() {
        return GroupedOpenApi.builder()
                .group("config")
                .pathsToMatch("/api/config/**")
                .build();
    }

    @Bean
    public GroupedOpenApi clusterApi() {
        return GroupedOpenApi.builder()
                .group("cluster")
                .pathsToMatch("/api/cluster/**")
                .build();
    }

    @Bean
    public GroupedOpenApi topicApi() {
        return GroupedOpenApi.builder()
                .group("topic")
                .pathsToMatch("/api/topic/**")
                .build();
    }

    @Bean
    public GroupedOpenApi consumerGroupApi() {
        return GroupedOpenApi.builder()
                .group("consumer-group")
                .pathsToMatch("/api/consumer-group/**")
                .build();
    }

    @Bean
    public GroupedOpenApi messageApi() {
        return GroupedOpenApi.builder()
                .group("message")
                .pathsToMatch("/api/message/**")
                .build();
    }

    @Bean
    public GroupedOpenApi monitorApi() {
        return GroupedOpenApi.builder()
                .group("monitor")
                .pathsToMatch("/api/monitor/**")
                .build();
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/api/**")
                .build();
    }
}
