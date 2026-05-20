package io.github.mykhailokulakov.genericspringservice.support.containers.postgres;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

@Target(TYPE)
@Retention(RUNTIME)
@Repeatable(WithPostgres.List.class)
@ExtendWith(PostgresExtension.class)
public @interface WithPostgres {

  String name() default "default";

  String image() default "postgres:17-alpine";

  @Target(TYPE)
  @Retention(RUNTIME)
  @ExtendWith(PostgresExtension.class)
  @interface List {
    WithPostgres[] value();
  }
}
