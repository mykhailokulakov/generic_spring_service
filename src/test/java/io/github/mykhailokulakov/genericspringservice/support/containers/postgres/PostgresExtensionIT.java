package io.github.mykhailokulakov.genericspringservice.support.containers.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PostgresExtensionIT {

  @Nested
  @WithPostgres
  class DefaultContainer {

    @Test
    void exportsStandardDatasourceProperties() {
      assertThat(System.getProperty("spring.datasource.url"))
          .as("default name maps to standard spring.datasource.* paths")
          .isNotNull()
          .startsWith("jdbc:postgresql://");
      assertThat(System.getProperty("spring.datasource.username")).isNotNull();
      assertThat(System.getProperty("spring.datasource.password")).isNotNull();
    }
  }

  @Nested
  @WithPostgres(name = "primary")
  @WithPostgres(name = "replica")
  class MultipleContainers {

    @Test
    void exportsPrefixedPropertiesForEachName() {
      String primaryUrl = System.getProperty("testcontainers.postgres.primary.url");
      String replicaUrl = System.getProperty("testcontainers.postgres.replica.url");

      assertThat(primaryUrl).isNotNull().startsWith("jdbc:postgresql://");
      assertThat(replicaUrl).isNotNull().startsWith("jdbc:postgresql://");
      assertThat(System.getProperty("testcontainers.postgres.primary.username")).isNotNull();
      assertThat(System.getProperty("testcontainers.postgres.primary.password")).isNotNull();
      assertThat(System.getProperty("testcontainers.postgres.replica.username")).isNotNull();
      assertThat(System.getProperty("testcontainers.postgres.replica.password")).isNotNull();

      assertThat(portOf(primaryUrl))
          .as("primary and replica must be distinct containers on different ports")
          .isNotEqualTo(portOf(replicaUrl));
    }

    private int portOf(String jdbcUrl) {
      // jdbc:postgresql://host:port/db
      String hostPort = jdbcUrl.substring("jdbc:postgresql://".length());
      String port = hostPort.substring(hostPort.indexOf(':') + 1, hostPort.indexOf('/'));
      return Integer.parseInt(port);
    }
  }
}
