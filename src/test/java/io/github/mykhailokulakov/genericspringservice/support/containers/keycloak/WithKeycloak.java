package io.github.mykhailokulakov.genericspringservice.support.containers.keycloak;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

@Target(TYPE)
@Retention(RUNTIME)
@Repeatable(WithKeycloak.List.class)
@ExtendWith(KeycloakExtension.class)
public @interface WithKeycloak {

  String name() default "default";

  String image() default "quay.io/keycloak/keycloak:26.0";

  String realmImport() default "keycloak/test-realm.json";

  @Target(TYPE)
  @Retention(RUNTIME)
  @ExtendWith(KeycloakExtension.class)
  @interface List {
    WithKeycloak[] value();
  }
}
