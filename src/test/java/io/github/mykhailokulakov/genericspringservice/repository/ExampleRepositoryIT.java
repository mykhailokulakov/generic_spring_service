package io.github.mykhailokulakov.genericspringservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.repository.specification.ExampleSpecifications;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class ExampleRepositoryIT extends AbstractRepositoryTestContract<ExampleEntity> {

  @Autowired private ExampleRepository repository;

  private static final Instant T0 = Instant.parse("2026-05-01T00:00:00Z");

  private ExampleEntity persist(
      String name, ExampleStatus status, BigDecimal price, Set<String> tags) {
    return repository.saveAndFlush(
        ExampleEntity.builder()
            .name(name)
            .status(status)
            .price(price)
            .occurredAt(T0)
            .tags(new HashSet<>(tags))
            .build());
  }

  @Test
  void emptyFilter_returnsAll() {
    persist("a", ExampleStatus.ACTIVE, new BigDecimal("1"), Set.of());
    persist("b", ExampleStatus.DRAFT, new BigDecimal("1"), Set.of());
    persist("c", ExampleStatus.ARCHIVED, new BigDecimal("1"), Set.of());

    var page =
        repository.findAll(
            ExampleSpecifications.matches(ExampleFilter.empty()), Pageable.unpaged());

    assertThat(page.getContent()).hasSize(3);
  }

  @Test
  void nullFilter_returnsAll() {
    persist("a", ExampleStatus.ACTIVE, new BigDecimal("1"), Set.of());
    persist("b", ExampleStatus.DRAFT, new BigDecimal("1"), Set.of());

    assertThat(repository.findAll(ExampleSpecifications.matches(null))).hasSize(2);
  }

  @Test
  void paginationAndSort_returnsRequestedSlice() {
    for (int i = 0; i < 7; i++) {
      repository.saveAndFlush(
          ExampleEntity.builder()
              .name("name-" + i)
              .status(ExampleStatus.ACTIVE)
              .price(new BigDecimal(i))
              .occurredAt(T0.plus(i, ChronoUnit.HOURS))
              .build());
    }

    var pageable = PageRequest.of(1, 3, Sort.by(Sort.Direction.ASC, "name"));
    var page = repository.findAll(ExampleSpecifications.matches(ExampleFilter.empty()), pageable);

    assertThat(page.getTotalElements()).isEqualTo(7);
    assertThat(page.getNumber()).isEqualTo(1);
    assertThat(page.getContent())
        .extracting(ExampleEntity::getName)
        .containsExactly("name-3", "name-4", "name-5");
  }

  @Test
  void combinedPredicates_intersectFilters() {
    persist("alpha", ExampleStatus.ACTIVE, new BigDecimal("10"), Set.of("red"));
    persist("alphabet", ExampleStatus.ACTIVE, new BigDecimal("10"), Set.of("red"));
    persist("alpha", ExampleStatus.DRAFT, new BigDecimal("10"), Set.of("red"));
    persist("beta", ExampleStatus.ACTIVE, new BigDecimal("10"), Set.of("red"));

    var filter =
        new ExampleFilter(
            "alpha", null, null, null, null, null, null, null,
            Set.of(ExampleStatus.ACTIVE), Set.of("red"));
    var page = repository.findAll(ExampleSpecifications.matches(filter), Pageable.unpaged());

    assertThat(page.getContent())
        .extracting(ExampleEntity::getName)
        .containsExactlyInAnyOrder("alpha", "alphabet");
  }

  @Test
  void nameContains_treatsPercentAndUnderscoreAsLiterals() {
    persist("100%", ExampleStatus.ACTIVE, new BigDecimal("1"), Set.of());
    persist("1000", ExampleStatus.ACTIVE, new BigDecimal("1"), Set.of());
    persist("a_b", ExampleStatus.ACTIVE, new BigDecimal("1"), Set.of());
    persist("axb", ExampleStatus.ACTIVE, new BigDecimal("1"), Set.of());

    var pct =
        repository.findAll(
            ExampleSpecifications.matches(
                new ExampleFilter("100%", null, null, null, null, null, null, null, null, null)),
            Pageable.unpaged());
    assertThat(pct.getContent()).extracting(ExampleEntity::getName).containsExactly("100%");

    var underscore =
        repository.findAll(
            ExampleSpecifications.matches(
                new ExampleFilter("a_b", null, null, null, null, null, null, null, null, null)),
            Pageable.unpaged());
    assertThat(underscore.getContent()).extracting(ExampleEntity::getName).containsExactly("a_b");
  }

  @Test
  void tagsContainsAny_doesNotInflatePageCountForMultiMatch() {
    persist("multi", ExampleStatus.ACTIVE, new BigDecimal("1"), Set.of("a", "b"));
    persist("single", ExampleStatus.ACTIVE, new BigDecimal("1"), Set.of("a"));

    var filter =
        new ExampleFilter(null, null, null, null, null, null, null, null, null, Set.of("a", "b"));
    var page = repository.findAll(ExampleSpecifications.matches(filter), PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.getContent())
        .extracting(ExampleEntity::getName)
        .containsExactlyInAnyOrder("multi", "single");
  }
}
