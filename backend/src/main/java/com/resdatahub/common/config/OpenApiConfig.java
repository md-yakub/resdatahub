package com.resdatahub.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI resDataHubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ResDataHub API")
                        .description("FAIR Research Data Repository API")
                        .version("1.0.0"));
    }
}
