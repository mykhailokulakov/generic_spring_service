package io.github.mykhailokulakov.genericspringservice.support;

import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asUnauthenticated;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asUser;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;

@IntegrationTest
class IntegrationTestSmokeIT {

  @LocalServerPort int port;
  @Autowired ApplicationContext context;

  @Test
  void givenIntegrationTestHarness_whenStarted_thenContextIsAvailableOnAPort() {
    assertThat(context).isNotNull();
    assertThat(port).isGreaterThan(0);
  }

  @Test
  void givenUnauthenticated_whenActuatorHealthCalled_thenReturns200() {
    asUnauthenticated().get("/actuator/health").then().statusCode(200);
  }

  @Test
  void givenUnauthenticated_whenApiCalled_thenReturns401() {
    asUnauthenticated().get("/api/v1/examples").then().statusCode(401);
  }

  @Test
  void givenUserToken_whenApiCalled_thenReturns200() {
    asUser().get("/api/v1/examples").then().statusCode(200);
  }
}
