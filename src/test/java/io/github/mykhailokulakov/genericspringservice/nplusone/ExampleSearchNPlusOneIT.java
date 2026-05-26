package io.github.mykhailokulakov.genericspringservice.nplusone;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.ExampleSpecifications;
import io.github.mykhailokulakov.genericspringservice.support.containers.postgres.WithPostgres;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration,"
          + "org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration"
    })
@ActiveProfiles("test")
@Transactional
@WithPostgres
class ExampleSearchNPlusOneIT {

  @Autowired ExampleRepository repository;
  @Autowired EntityManager em;

  @BeforeEach
  void seed() {
    em.createNativeQuery("DELETE FROM example_tag").executeUpdate();
    em.createNativeQuery("DELETE FROM example").executeUpdate();
    for (int i = 0; i < 20; i++) {
      var e =
          ExampleEntity.builder()
              .name("entity-" + i)
              .quantity(i)
              .price(new BigDecimal(i))
              .occurredAt(Instant.parse("2026-05-01T00:00:00Z"))
              .status(ExampleStatus.ACTIVE)
              .tags(new HashSet<>(Set.of("tag-a-" + i, "tag-b-" + i, "tag-c-" + i)))
              .build();
      em.persist(e);
    }
    em.flush();
    em.clear();
  }

  @Test
  void searchPageOfTen_doesNotIssueNPlusOneQueries() {
    var sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactory.class);
    var stats = sessionFactory.getStatistics();
    stats.clear();

    var pageable = PageRequest.of(0, 10);
    var page =
        repository.findAll(
            ExampleSpecifications.matches(
                null, null, null, null, null, null, null, null, null, null, null),
            pageable);

    int totalTags = 0;
    for (ExampleEntity e : page.getContent()) {
      totalTags += e.getTags().size();
    }

    assertThat(page.getContent()).hasSize(10);
    assertThat(totalTags).isEqualTo(30);
    // 1: SELECT count(*) ... (pagination total)
    // 2: SELECT id, name, ... FROM example ... LIMIT 10 (page data)
    // 3: SELECT ... FROM example_tag WHERE example_id IN (...) (@BatchSize batch)
    long maxExpectedStatements = 3L;
    assertThat(stats.getPrepareStatementCount())
        .as("paging 10 of 20 with tag access must stay bounded")
        .isLessThanOrEqualTo(maxExpectedStatements);
    // Pins pagination to SQL level. If a fetch-join on `tags` ever sneaks back
    // in, Hibernate would load all 20 entities and paginate in memory — this
    // assertion fails at 20 instead of 10.
    assertThat(stats.getEntityLoadCount())
        .as("only the page's entities should be loaded; in-memory pagination would load all 20")
        .isEqualTo(10L);
  }
}
