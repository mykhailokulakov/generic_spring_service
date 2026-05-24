package io.github.mykhailokulakov.genericspringservice.support.containers.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KeycloakExtensionIT {

  @Nested
  @WithKeycloak
  class DefaultContainer {

    @Test
    void exportsStandardIssuerUriProperty() {
      String issuer = System.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
      assertThat(issuer)
          .as("default name maps to standard spring.security.oauth2.* path")
          .isNotNull()
          .startsWith("http")
          .endsWith("/realms/generic");
    }

    @Test
    void issuesValidJwtForSeededAdmin() {
      var factory = TestJwtFactory.forDefaultContainer();
      String token = factory.adminToken();
      assertThat(token).isNotBlank();
      // JWT = header.payload.signature
      assertThat(token.split("\\.")).hasSize(3);
    }
  }

  @Nested
  @WithKeycloak(name = "primary")
  @WithKeycloak(name = "replica")
  class MultipleContainers {

    @Test
    void exportsPrefixedPropertiesForEachName() {
      String primary = System.getProperty("testcontainers.keycloak.primary.issuer-uri");
      String replica = System.getProperty("testcontainers.keycloak.replica.issuer-uri");

      assertThat(primary).isNotNull().endsWith("/realms/generic");
      assertThat(replica).isNotNull().endsWith("/realms/generic");
      assertThat(System.getProperty("testcontainers.keycloak.primary.auth-server-url")).isNotNull();
      assertThat(System.getProperty("testcontainers.keycloak.replica.auth-server-url")).isNotNull();

      assertThat(portOf(primary))
          .as("primary and replica must be distinct containers on different ports")
          .isNotEqualTo(portOf(replica));
    }

    private int portOf(String issuerUri) {
      return URI.create(issuerUri).getPort();
    }
  }
}
