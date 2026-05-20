package io.github.mykhailokulakov.genericspringservice.nplusone;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.ExampleSpecifications;
import io.github.mykhailokulakov.genericspringservice.support.containers.postgres.WithPostgres;
import io.github.mykhailokulakov.genericspringservice.web.dto.ExampleFilter;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
      ExampleEntity e =
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
    SessionFactory sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactory.class);
    Statistics stats = sessionFactory.getStatistics();
    stats.clear();

    Pageable pageable = PageRequest.of(0, 10);
    Page<ExampleEntity> page =
        repository.findAll(ExampleSpecifications.matches(ExampleFilter.empty()), pageable);

    int totalTags = 0;
    for (ExampleEntity e : page.getContent()) {
      totalTags += e.getTags().size();
    }

    assertThat(page.getContent()).hasSize(10);
    assertThat(totalTags).isEqualTo(30);
    assertThat(stats.getPrepareStatementCount())
        .as("paging 10 of 20 with tag access must stay bounded (1 count + 1 page + 1 batch tags)")
        .isLessThanOrEqualTo(3L);
    // Pins pagination to SQL level. If a fetch-join on `tags` ever sneaks back
    // in, Hibernate would load all 20 entities and paginate in memory — this
    // assertion fails at 20 instead of 10.
    assertThat(stats.getEntityLoadCount())
        .as("only the page's entities should be loaded; in-memory pagination would load all 20")
        .isEqualTo(10L);
  }
}
