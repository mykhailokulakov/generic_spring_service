package io.github.mykhailokulakov.genericspringservice.support.db;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.support.IntegrationTest;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@IntegrationTest
@Import(DatabaseStateHelper.class)
class DatabaseStateHelperIT {

  @LocalServerPort int port;
  @Autowired DatabaseStateHelper db;
  @Autowired ExampleRepository repository;

  @BeforeEach
  void clean() {
    db.truncateAll();
  }

  @Test
  void countsIncludeAndExcludeSoftDeleted() {
    var kept = repository.save(newExample("kept"));
    var deleted = repository.save(newExample("deleted"));
    repository.delete(deleted);

    assertThat(db.countIncludingDeleted(ExampleEntity.class)).isEqualTo(2L);
    assertThat(db.countWhereDeleted(ExampleEntity.class)).isEqualTo(1L);
    assertThat(repository.findById(kept.getId())).isPresent();
    assertThat(repository.findById(deleted.getId())).isEmpty();
  }

  @Test
  void truncateAllWipesEveryRow() {
    repository.save(newExample("one"));
    repository.save(newExample("two"));
    repository.delete(repository.save(newExample("three")));

    db.truncateAll();

    assertThat(db.countIncludingDeleted(ExampleEntity.class)).isEqualTo(0L);
  }

  private static ExampleEntity newExample(String name) {
    return ExampleEntity.builder()
        .name(name)
        .description("desc-" + name)
        .quantity(1)
        .price(new BigDecimal("1.00"))
        .occurredAt(Instant.now())
        .status(ExampleStatus.ACTIVE)
        .build();
  }
}
