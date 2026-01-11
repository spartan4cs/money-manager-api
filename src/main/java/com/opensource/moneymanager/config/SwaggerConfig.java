package com.opensource.moneymanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI configuration for the Money Manager API.
 * Provides custom API documentation metadata and styling.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configure OpenAPI documentation.
     * This bean customizes the Swagger/OpenAPI UI with API information,
     * contact details, and license information.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Money Manager API")
                        .version("1.0.0")
                        .description("REST API for managing accounts and transactions. " +
                                "Provides comprehensive endpoints for account management, " +
                                "transaction tracking, and financial analytics.")
                        .contact(new Contact()
                                .name("Money Manager Team")
                                .url("https://github.com/spartan4cs/money-manager-api")
                                .email("support@moneymanager.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

