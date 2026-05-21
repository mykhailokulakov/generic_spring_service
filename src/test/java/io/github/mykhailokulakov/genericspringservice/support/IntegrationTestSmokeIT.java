package io.github.mykhailokulakov.genericspringservice.support;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.support.containers.keycloak.TestJwtFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;

@IntegrationTest
class IntegrationTestSmokeIT {

  @LocalServerPort int port;
  @Autowired ApplicationContext context;
  @Autowired TestJwtFactory jwt;

  @Test
  void contextStarts() {
    assertThat(context).isNotNull();
    assertThat(port).isGreaterThan(0);
  }

  @Test
  void actuatorHealthIsPublic() {
    given().when().get("/actuator/health").then().statusCode(200);
  }

  @Test
  void apiRequiresAuthentication() {
    given().when().get("/api/v1/examples").then().statusCode(401);
  }

  @Test
  void apiAcceptsUserToken() {
    given()
        .header("Authorization", "Bearer " + jwt.userToken())
        .when()
        .get("/api/v1/examples")
        .then()
        .statusCode(200);
  }
}
