package io.github.mykhailokulakov.genericspringservice.support.auth;

import static io.restassured.RestAssured.given;

import io.github.mykhailokulakov.genericspringservice.support.containers.keycloak.TestJwtFactory;
import io.restassured.specification.RequestSpecification;
import java.util.concurrent.atomic.AtomicReference;

public final class RestAssuredAuth {

  private static final AtomicReference<TestJwtFactory> JWT_FACTORY = new AtomicReference<>();

  private RestAssuredAuth() {}

  public static RequestSpecification asAdmin() {
    return given().auth().oauth2(requireFactory().adminToken());
  }

  public static RequestSpecification asUser() {
    return given().auth().oauth2(requireFactory().userToken());
  }

  public static RequestSpecification asUnauthenticated() {
    return given();
  }

  public static RequestSpecification withToken(String token) {
    return given().auth().oauth2(token);
  }

  /**
   * Extension hook: wires the {@link TestJwtFactory} that {@link #asAdmin()} / {@link #asUser()}
   * delegate to. Called by {@code KeycloakExtension} during {@code BeforeAllCallback}, and again
   * with {@code null} in {@code AfterAllCallback} to prevent leakage between test classes. Not for
   * direct test use — tests should call the {@code as*()} methods instead.
   */
  public static void setJwtFactory(TestJwtFactory factory) {
    JWT_FACTORY.set(factory);
  }

  private static TestJwtFactory requireFactory() {
    var factory = JWT_FACTORY.get();
    if (factory == null) {
      throw new IllegalStateException(
          "RestAssuredAuth has no TestJwtFactory — is @WithKeycloak on the test class?");
    }
    return factory;
  }
}
