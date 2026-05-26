package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.support.PersistenceTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

@PersistenceTest
@Import(DatabaseStateHelper.class)
class ExampleSpecificationsIT {

  @Autowired private ExampleRepository repository;
  @Autowired private DatabaseStateHelper db;

  private static final Instant T0 = Instant.parse("2026-05-01T00:00:00Z");

  @BeforeEach
  void clean() {
    db.truncateAll();
  }

  private ExampleEntity persist(String name, ExampleStatus status, BigDecimal price) {
    return repository.saveAndFlush(
        ExampleEntity.builder()
            .name(name)
            .status(status)
            .price(price)
            .occurredAt(T0)
            .tags(new HashSet<>())
            .build());
  }

  @Test
  void allNullParams_returnsAll() {
    persist("a", ExampleStatus.ACTIVE, BigDecimal.ONE);
    persist("b", ExampleStatus.DRAFT, BigDecimal.TEN);
    var result =
        repository.findAll(
            ExampleSpecifications.matches(
                null, null, null, null, null, null, null, null, null, null, null),
            Pageable.unpaged());
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void idIn_filtersById() {
    var a = persist("a", ExampleStatus.ACTIVE, BigDecimal.ONE);
    persist("b", ExampleStatus.ACTIVE, BigDecimal.ONE);
    var result =
        repository.findAll(
            ExampleSpecifications.matches(
                List.of(a.getId()), null, null, null, null, null, null, null, null, null, null),
            Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getName()).isEqualTo("a");
  }

  @Test
  void nameContains_matchesCaseInsensitively() {
    persist("Alpha", ExampleStatus.ACTIVE, BigDecimal.ONE);
    persist("Beta", ExampleStatus.ACTIVE, BigDecimal.ONE);
    var result =
        repository.findAll(
            ExampleSpecifications.matches(
                null, "alpha", null, null, null, null, null, null, null, null, null),
            Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getName()).isEqualTo("Alpha");
  }

  @Test
  void statusIn_filtersByStatus() {
    persist("a", ExampleStatus.ACTIVE, BigDecimal.ONE);
    persist("b", ExampleStatus.DRAFT, BigDecimal.ONE);
    persist("c", ExampleStatus.ARCHIVED, BigDecimal.ONE);
    var result =
        repository.findAll(
            ExampleSpecifications.matches(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of(ExampleStatus.ACTIVE, ExampleStatus.DRAFT),
                null),
            Pageable.unpaged());
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void priceBetween_returnsRowsInRange() {
    persist("cheap", ExampleStatus.ACTIVE, new BigDecimal("5"));
    persist("mid", ExampleStatus.ACTIVE, new BigDecimal("15"));
    persist("expensive", ExampleStatus.ACTIVE, new BigDecimal("25"));
    var result =
        repository.findAll(
            ExampleSpecifications.matches(
                null,
                null,
                null,
                null,
                null,
                new BigDecimal("10"),
                new BigDecimal("20"),
                null,
                null,
                null,
                null),
            Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getName()).isEqualTo("mid");
  }

  @Test
  void tagsContainsAny_returnsMatchingRows() {
    var a =
        repository.saveAndFlush(
            ExampleEntity.builder()
                .name("a")
                .status(ExampleStatus.ACTIVE)
                .price(BigDecimal.ONE)
                .occurredAt(T0)
                .tags(new HashSet<>(Set.of("red")))
                .build());
    repository.saveAndFlush(
        ExampleEntity.builder()
            .name("b")
            .status(ExampleStatus.ACTIVE)
            .price(BigDecimal.ONE)
            .occurredAt(T0)
            .tags(new HashSet<>(Set.of("blue")))
            .build());
    var result =
        repository.findAll(
            ExampleSpecifications.matches(
                null, null, null, null, null, null, null, null, null, null, Set.of("red")),
            Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getId()).isEqualTo(a.getId());
  }
}
