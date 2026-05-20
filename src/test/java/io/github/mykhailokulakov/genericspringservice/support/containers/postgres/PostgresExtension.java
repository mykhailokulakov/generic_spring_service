package io.github.mykhailokulakov.genericspringservice.support.containers.postgres;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresExtension implements BeforeAllCallback {

  private static final Map<String, PostgreSQLContainer<?>> CONTAINERS = new ConcurrentHashMap<>();

  @Override
  public void beforeAll(ExtensionContext ctx) {
    Class<?> testClass = ctx.getRequiredTestClass();
    WithPostgres[] declarations = collectAnnotations(testClass);
    if (declarations.length == 0) {
      declarations = new WithPostgres[] {defaults()};
    }

    for (WithPostgres decl : declarations) {
      PostgreSQLContainer<?> container =
          CONTAINERS.computeIfAbsent(
              decl.name(),
              name ->
                  new PostgreSQLContainer<>(DockerImageName.parse(decl.image()))
                      .withLabel("tc.name", name)
                      .withReuse(true));
      if (!container.isRunning()) {
        container.start();
      }
      exportProperties(decl.name(), container);
    }
  }

  private void exportProperties(String name, PostgreSQLContainer<?> container) {
    if ("default".equals(name)) {
      System.setProperty("spring.datasource.url", container.getJdbcUrl());
      System.setProperty("spring.datasource.username", container.getUsername());
      System.setProperty("spring.datasource.password", container.getPassword());
    } else {
      String prefix = "testcontainers.postgres." + name + ".";
      System.setProperty(prefix + "url", container.getJdbcUrl());
      System.setProperty(prefix + "username", container.getUsername());
      System.setProperty(prefix + "password", container.getPassword());
    }
  }

  private WithPostgres[] collectAnnotations(Class<?> testClass) {
    return testClass.getAnnotationsByType(WithPostgres.class);
  }

  private static WithPostgres defaults() {
    return new WithPostgres() {
      @Override
      public String name() {
        return "default";
      }

      @Override
      public String image() {
        return "postgres:17-alpine";
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return WithPostgres.class;
      }
    };
  }
}
