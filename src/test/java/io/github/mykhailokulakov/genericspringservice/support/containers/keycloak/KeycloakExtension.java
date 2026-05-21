package io.github.mykhailokulakov.genericspringservice.support.containers.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.utility.DockerImageName;

public class KeycloakExtension implements BeforeAllCallback {

  private static final Map<ContainerKey, Holder> CONTAINERS = new ConcurrentHashMap<>();
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public void beforeAll(ExtensionContext ctx) {
    Class<?> testClass = ctx.getRequiredTestClass();
    WithKeycloak[] declarations = testClass.getAnnotationsByType(WithKeycloak.class);
    if (declarations.length == 0) {
      declarations = new WithKeycloak[] {defaults()};
    }

    for (WithKeycloak decl : declarations) {
      ContainerKey key = new ContainerKey(decl.name(), decl.image(), decl.realmImport());
      Holder holder =
          CONTAINERS.computeIfAbsent(
              key,
              k -> {
                KeycloakContainer container =
                    new KeycloakContainer(
                            DockerImageName.parse(k.image())
                                .asCompatibleSubstituteFor("quay.io/keycloak/keycloak")
                                .toString())
                        .withRealmImportFile(k.realmImport())
                        .withReuse(true);
                container.withLabel("tc.name", k.name());
                return new Holder(container, readRealmName(k.realmImport()));
              });
      if (!holder.container.isRunning()) {
        holder.container.start();
      }
      exportProperties(decl.name(), holder);
    }
  }

  private void exportProperties(String name, Holder holder) {
    String authServerUrl = holder.container.getAuthServerUrl();
    String base = authServerUrl.endsWith("/") ? authServerUrl : authServerUrl + "/";
    String issuerUri = base + "realms/" + holder.realmName;
    if ("default".equals(name)) {
      System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", issuerUri);
      System.setProperty("testcontainers.keycloak.default.issuer-uri", issuerUri);
      System.setProperty("testcontainers.keycloak.default.auth-server-url", authServerUrl);
      System.setProperty("testcontainers.keycloak.default.realm", holder.realmName);
    } else {
      String prefix = "testcontainers.keycloak." + name + ".";
      System.setProperty(prefix + "issuer-uri", issuerUri);
      System.setProperty(prefix + "auth-server-url", authServerUrl);
      System.setProperty(prefix + "realm", holder.realmName);
    }
  }

  private static String readRealmName(String classpathLocation) {
    try (InputStream in =
        KeycloakExtension.class.getClassLoader().getResourceAsStream(classpathLocation)) {
      if (in == null) {
        throw new IllegalArgumentException(
            "Realm import not found on classpath: " + classpathLocation);
      }
      JsonNode root = MAPPER.readTree(in);
      JsonNode realm = root.get("realm");
      if (realm == null || realm.isNull()) {
        throw new IllegalArgumentException(
            "Realm import is missing \"realm\" field: " + classpathLocation);
      }
      return realm.asText();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read realm import: " + classpathLocation, e);
    }
  }

  private static WithKeycloak defaults() {
    return new WithKeycloak() {
      @Override
      public String name() {
        return "default";
      }

      @Override
      public String image() {
        return "quay.io/keycloak/keycloak:26.0";
      }

      @Override
      public String realmImport() {
        return "keycloak/test-realm.json";
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return WithKeycloak.class;
      }
    };
  }

  private record ContainerKey(String name, String image, String realmImport) {}

  private record Holder(KeycloakContainer container, String realmName) {}
}
