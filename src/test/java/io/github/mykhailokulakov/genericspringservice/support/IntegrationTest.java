package io.github.mykhailokulakov.genericspringservice.support;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.github.mykhailokulakov.genericspringservice.support.containers.keycloak.WithKeycloak;
import io.github.mykhailokulakov.genericspringservice.support.containers.postgres.WithPostgres;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Target(TYPE)
@Retention(RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@WithPostgres
@WithKeycloak
@ExtendWith(RestAssuredExtension.class)
public @interface IntegrationTest {}
