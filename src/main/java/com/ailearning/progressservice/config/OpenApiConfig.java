package com.ailearning.progressservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI progressServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EduGen-AI Progress Tracking API")
                        .description("REST API for tracking student progress, subject mastery, module quest levels, activity submissions, and aggregate analytics.")
                        .version("v1.0")
                        .contact(new Contact().name("EduGen-AI Team")));
    }
}
