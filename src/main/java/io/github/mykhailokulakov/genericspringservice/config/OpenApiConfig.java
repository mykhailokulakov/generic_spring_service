package io.github.mykhailokulakov.genericspringservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String SECURITY_SCHEME_NAME = "bearerAuth";

  @Bean
  public OpenAPI openAPI(
      @Value("${spring.application.name:generic-spring-service}") String applicationName,
      ObjectProvider<BuildProperties> buildPropertiesProvider) {
    BuildProperties buildProperties = buildPropertiesProvider.getIfAvailable();
    String title = buildProperties != null ? buildProperties.getName() : applicationName;
    String version = buildProperties != null ? buildProperties.getVersion() : "dev";

    return new OpenAPI()
        .info(new Info().title(title).version(version).description("REST API for " + title + "."))
        .addServersItem(new Server().url("/").description("Default"))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        .components(
            new Components()
                .addSecuritySchemes(
                    SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT bearer token issued by Keycloak.")));
  }
}
