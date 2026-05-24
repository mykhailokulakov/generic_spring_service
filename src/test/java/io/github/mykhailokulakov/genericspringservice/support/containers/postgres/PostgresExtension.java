package io.github.mykhailokulakov.genericspringservice.support.containers.postgres;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresExtension implements BeforeAllCallback {

  private static final Map<ContainerKey, PostgreSQLContainer<?>> CONTAINERS =
      new ConcurrentHashMap<>();

  @Override
  public void beforeAll(ExtensionContext ctx) {
    var testClass = ctx.getRequiredTestClass();
    WithPostgres[] declarations = collectAnnotations(testClass);
    if (declarations.length == 0) {
      declarations = new WithPostgres[] {defaults()};
    }

    for (WithPostgres decl : declarations) {
      var key = new ContainerKey(decl.name(), decl.image());
      PostgreSQLContainer<?> container =
          CONTAINERS.computeIfAbsent(
              key,
              k ->
                  new PostgreSQLContainer<>(DockerImageName.parse(k.image()))
                      .withLabel("tc.name", k.name())
                      .withReuse(true));
      if (!container.isRunning()) {
        container.start();
      }
      exportProperties(decl.name(), container);
    }
  }

  private record ContainerKey(String name, String image) {}

  private void exportProperties(String name, PostgreSQLContainer<?> container) {
    if ("default".equals(name)) {
      System.setProperty("spring.datasource.url", container.getJdbcUrl());
      System.setProperty("spring.datasource.username", container.getUsername());
      System.setProperty("spring.datasource.password", container.getPassword());
    } else {
      var prefix = "testcontainers.postgres." + name + ".";
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
