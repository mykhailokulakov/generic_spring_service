package io.github.mykhailokulakov.genericspringservice.support;

import io.github.mykhailokulakov.genericspringservice.support.containers.postgres.WithPostgres;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration,"
          + "org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration"
    })
@ActiveProfiles("test")
@WithPostgres
public @interface PersistenceTest {}
