package io.github.mykhailokulakov.genericspringservice.support.containers.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Issues JWTs from the in-test Keycloak via the OAuth2 password grant. Tokens are cached per
 * (username, password) for the JVM lifetime — the seeded test realm uses a 1h access token
 * lifespan, well beyond any single test run.
 */
@Component
public class TestJwtFactory {

  public static final String DEFAULT_CLIENT_ID = "generic-spring-service";
  public static final String DEFAULT_CLIENT_SECRET = "generic-spring-service-secret";
  public static final String ADMIN_USERNAME = "admin";
  public static final String ADMIN_PASSWORD = "admin";
  public static final String USER_USERNAME = "user";
  public static final String USER_PASSWORD = "user";

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final HttpClient HTTP =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  private final String tokenUrl;
  private final String clientId;
  private final String clientSecret;
  private final Map<CacheKey, String> cache = new ConcurrentHashMap<>();

  private record CacheKey(String username, String password) {}

  public TestJwtFactory(
      @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
      @Value("${test.keycloak.client-id:" + DEFAULT_CLIENT_ID + "}") String clientId,
      @Value("${test.keycloak.client-secret:" + DEFAULT_CLIENT_SECRET + "}") String clientSecret) {
    this.tokenUrl = issuerUri + "/protocol/openid-connect/token";
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  /** Build a factory pointed at the default {@code @WithKeycloak} container without Spring DI. */
  public static TestJwtFactory forDefaultContainer() {
    return forContainer("default");
  }

  /**
   * Build a factory pointed at a named {@code @WithKeycloak} container without Spring DI. Pass
   * {@code "default"} for the unnamed container.
   */
  public static TestJwtFactory forContainer(String name) {
    String property =
        "default".equals(name)
            ? "spring.security.oauth2.resourceserver.jwt.issuer-uri"
            : "testcontainers.keycloak." + name + ".issuer-uri";
    String issuerUri = System.getProperty(property);
    if (issuerUri == null) {
      throw new IllegalStateException(
          property
              + " is not set — did you forget @WithKeycloak(name=\""
              + name
              + "\") on the test class?");
    }
    return new TestJwtFactory(issuerUri, DEFAULT_CLIENT_ID, DEFAULT_CLIENT_SECRET);
  }

  public String tokenFor(String username, String password) {
    return cache.computeIfAbsent(new CacheKey(username, password), k -> fetch(username, password));
  }

  public String adminToken() {
    return tokenFor(ADMIN_USERNAME, ADMIN_PASSWORD);
  }

  public String userToken() {
    return tokenFor(USER_USERNAME, USER_PASSWORD);
  }

  private String fetch(String username, String password) {
    String form =
        "grant_type=password"
            + "&client_id="
            + enc(clientId)
            + "&client_secret="
            + enc(clientSecret)
            + "&username="
            + enc(username)
            + "&password="
            + enc(password);
    var req =
        HttpRequest.newBuilder(URI.create(tokenUrl))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();
    try {
      HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() / 100 != 2) {
        throw new IllegalStateException(
            "Keycloak token request failed ("
                + res.statusCode()
                + ") for user "
                + username
                + ": "
                + res.body());
      }
      JsonNode body = MAPPER.readTree(res.body());
      JsonNode token = body.get("access_token");
      if (token == null || token.isNull()) {
        throw new IllegalStateException("Token response missing access_token: " + res.body());
      }
      return token.asText();
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Failed to fetch token for " + username, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted fetching token for " + username, e);
    }
  }

  private static String enc(String v) {
    return URLEncoder.encode(v, StandardCharsets.UTF_8);
  }
}
