package io.github.mykhailokulakov.genericspringservice.support.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.support.IntegrationTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@IntegrationTest
@Import(DatabaseStateHelper.class)
@WithSeededExamples(count = 5)
class SeededExamplesExtensionIT {

  @LocalServerPort int port;
  @Autowired ExampleRepository repository;
  @Autowired DatabaseStateHelper db;

  @Test
  void seedsExactlyTheRequestedRowCountBeforeEachTest() {
    assertThat(repository.findAll()).hasSize(5);
    assertThat(repository.findAll())
        .allSatisfy(e -> assertThat(e.getStatus()).isEqualTo(ExampleStatus.ACTIVE))
        .allSatisfy(e -> assertThat(e.getName()).startsWith("example-"));
  }

  @RepeatedTest(3)
  void truncateResetsRowsBeforeEachInvocation() {
    // Mutate state — the extension must wipe + reseed before each repetition.
    repository.save(ExampleEntity.builder().name("intruder").status(ExampleStatus.ACTIVE).build());
    repository.flush();

    long total = db.countIncludingDeleted(ExampleEntity.class);
    assertThat(total).isEqualTo(6L);
  }
}
