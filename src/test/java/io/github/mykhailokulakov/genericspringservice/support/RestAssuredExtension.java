package io.github.mykhailokulakov.genericspringservice.support;

import io.restassured.RestAssured;
import java.lang.reflect.Field;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.web.server.LocalServerPort;

public class RestAssuredExtension implements BeforeEachCallback, AfterAllCallback {

  @Override
  public void beforeEach(ExtensionContext ctx) throws Exception {
    Object instance =
        ctx.getRequiredTestInstances().getAllInstances().stream()
            .filter(i -> findPortField(i.getClass()) != null)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "RestAssuredExtension requires a field annotated with @LocalServerPort"
                            + " on the test class (or an enclosing instance)"));

    Field field = findPortField(instance.getClass());
    field.setAccessible(true);
    int port = field.getInt(instance);

    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  @Override
  public void afterAll(ExtensionContext ctx) {
    RestAssured.reset();
  }

  private static Field findPortField(Class<?> type) {
    for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
      for (Field f : c.getDeclaredFields()) {
        if (f.isAnnotationPresent(LocalServerPort.class)) {
          return f;
        }
      }
    }
    return null;
  }
}
