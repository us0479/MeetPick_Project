package com.meetpick.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [COMMON BACKBONE]
 * A/B/C가 같은 Swagger 문서에서 API를 검증하도록 OpenAPI 공통 정보를 정의합니다.
 * bearerAuth는 A의 JWT 구현 이후 각 보호 API에 적용됩니다.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI meetpickOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MeetPick API")
                        .version("v0.1")
                        .description("친구/팀 약속 일정 자동 조율 서비스 API"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
