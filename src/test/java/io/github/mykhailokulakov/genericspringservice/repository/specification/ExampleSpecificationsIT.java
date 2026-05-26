package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.support.PersistenceTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@PersistenceTest
@Import(DatabaseStateHelper.class)
class ExampleSpecificationsIT {

  @Autowired private ExampleRepository repository;
  @Autowired private DatabaseStateHelper dbHelper;

  private static final Instant T0 = Instant.parse("2026-05-01T00:00:00Z");

  @BeforeEach
  void cleanDb() {
    dbHelper.truncateAll();
  }

  private ExampleEntity persist(
      String name,
      String description,
      Integer quantity,
      BigDecimal price,
      Instant occurredAt,
      ExampleStatus status,
      Set<String> tags) {
    var entity =
        ExampleEntity.builder()
            .name(name)
            .description(description)
            .quantity(quantity)
            .price(price)
            .occurredAt(occurredAt)
            .status(status)
            .tags(new HashSet<>(tags))
            .build();
    return repository.saveAndFlush(entity);
  }

  @Test
  void nameContains_matchesCaseInsensitively() {
    persist("Alpha widget", "x", 1, new BigDecimal("1.00"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("Beta gadget", "y", 1, new BigDecimal("1.00"), T0, ExampleStatus.ACTIVE, Set.of());

    var filter = new ExampleFilter("ALPHA", null, null, null, null, null, null, null, null, null);
    var page = repository.findAll(ExampleSpecifications.matches(filter), Pageable.unpaged());

    assertThat(page.getContent())
        .hasSize(1)
        .first()
        .extracting(ExampleEntity::getName)
        .isEqualTo("Alpha widget");
  }

  @Test
  void priceBetween_returnsRowsInRange() {
    persist("a", null, 1, new BigDecimal("5.00"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("b", null, 1, new BigDecimal("15.00"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("c", null, 1, new BigDecimal("25.00"), T0, ExampleStatus.ACTIVE, Set.of());

    var filter =
        new ExampleFilter(
            null,
            null,
            null,
            null,
            new BigDecimal("10.00"),
            new BigDecimal("20.00"),
            null,
            null,
            null,
            null);
    var page = repository.findAll(ExampleSpecifications.matches(filter), Pageable.unpaged());

    assertThat(page.getContent()).extracting(ExampleEntity::getName).containsExactly("b");
  }

  @Test
  void statusIn_filtersByStatuses() {
    persist("a", null, 1, new BigDecimal("1"), T0, ExampleStatus.DRAFT, Set.of());
    persist("b", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("c", null, 1, new BigDecimal("1"), T0, ExampleStatus.ARCHIVED, Set.of());

    var filter =
        new ExampleFilter(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Set.of(ExampleStatus.DRAFT, ExampleStatus.ACTIVE),
            null);
    var page = repository.findAll(ExampleSpecifications.matches(filter), Pageable.unpaged());

    assertThat(page.getContent())
        .extracting(ExampleEntity::getName)
        .containsExactlyInAnyOrder("a", "b");
  }

  @Test
  void tagsContainsAny_returnsRowsWithAnyMatchingTag() {
    persist("a", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of("red"));
    persist("b", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of("blue"));
    persist("c", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of("green"));

    var filter =
        new ExampleFilter(
            null, null, null, null, null, null, null, null, null, Set.of("red", "green"));
    var page = repository.findAll(ExampleSpecifications.matches(filter), Pageable.unpaged());

    assertThat(page.getContent())
        .extracting(ExampleEntity::getName)
        .containsExactlyInAnyOrder("a", "c");
  }

  @Test
  void combinedPredicates_intersectFilters() {
    persist("alpha", null, 5, new BigDecimal("10"), T0, ExampleStatus.ACTIVE, Set.of("red"));
    persist("alphabet", null, 50, new BigDecimal("10"), T0, ExampleStatus.ACTIVE, Set.of("red"));
    persist("alpha", null, 5, new BigDecimal("10"), T0, ExampleStatus.DRAFT, Set.of("red"));
    persist("beta", null, 5, new BigDecimal("10"), T0, ExampleStatus.ACTIVE, Set.of("red"));

    var filter =
        new ExampleFilter(
            "alpha",
            null,
            1,
            10,
            null,
            null,
            null,
            null,
            Set.of(ExampleStatus.ACTIVE),
            Set.of("red"));
    var page = repository.findAll(ExampleSpecifications.matches(filter), Pageable.unpaged());

    assertThat(page.getContent())
        .hasSize(1)
        .first()
        .satisfies(
            e -> {
              assertThat(e.getName()).isEqualTo("alpha");
              assertThat(e.getQuantity()).isEqualTo(5);
              assertThat(e.getStatus()).isEqualTo(ExampleStatus.ACTIVE);
            });
  }

  @Test
  void emptyFilter_returnsAll() {
    persist("a", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("b", null, 1, new BigDecimal("1"), T0, ExampleStatus.DRAFT, Set.of());
    persist("c", null, 1, new BigDecimal("1"), T0, ExampleStatus.ARCHIVED, Set.of());

    var page =
        repository.findAll(
            ExampleSpecifications.matches(ExampleFilter.empty()), Pageable.unpaged());

    assertThat(page.getContent()).hasSize(3);
  }

  @Test
  void paginationAndSort_returnsRequestedSlice() {
    for (int i = 0; i < 7; i++) {
      persist(
          "name-" + i,
          null,
          i,
          new BigDecimal(i),
          T0.plus(i, ChronoUnit.HOURS),
          ExampleStatus.ACTIVE,
          Set.of());
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
  void quantityRange_minOnlyAndMaxOnly() {
    persist("a", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("b", null, 5, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("c", null, 10, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());

    var minOnly = new ExampleFilter(null, null, 5, null, null, null, null, null, null, null);
    var minPage = repository.findAll(ExampleSpecifications.matches(minOnly), Pageable.unpaged());
    assertThat(minPage.getContent())
        .extracting(ExampleEntity::getName)
        .containsExactlyInAnyOrder("b", "c");

    var maxOnly = new ExampleFilter(null, null, null, 5, null, null, null, null, null, null);
    var maxPage = repository.findAll(ExampleSpecifications.matches(maxOnly), Pageable.unpaged());
    assertThat(maxPage.getContent())
        .extracting(ExampleEntity::getName)
        .containsExactlyInAnyOrder("a", "b");
  }

  @Test
  void occurredBetween_filtersByRange() {
    persist("a", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());
    persist(
        "b",
        null,
        1,
        new BigDecimal("1"),
        T0.plus(2, ChronoUnit.DAYS),
        ExampleStatus.ACTIVE,
        Set.of());
    persist(
        "c",
        null,
        1,
        new BigDecimal("1"),
        T0.plus(10, ChronoUnit.DAYS),
        ExampleStatus.ACTIVE,
        Set.of());

    var filter =
        new ExampleFilter(
            null,
            null,
            null,
            null,
            null,
            null,
            T0.plus(1, ChronoUnit.DAYS),
            T0.plus(5, ChronoUnit.DAYS),
            null,
            null);
    var page = repository.findAll(ExampleSpecifications.matches(filter), Pageable.unpaged());

    assertThat(page.getContent()).extracting(ExampleEntity::getName).containsExactly("b");
  }

  @Test
  void descriptionContains_matchesCaseInsensitively() {
    persist(
        "a", "The quick brown fox", 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("b", "Lazy dog", 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());

    var filter = new ExampleFilter(null, "QUICK", null, null, null, null, null, null, null, null);
    var page = repository.findAll(ExampleSpecifications.matches(filter), Pageable.unpaged());

    assertThat(page.getContent()).extracting(ExampleEntity::getName).containsExactly("a");
  }

  @Test
  void tagsContainsAny_doesNotInflatePageCountForMultiMatch() {
    persist("multi", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of("a", "b"));
    persist("single", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of("a"));

    var filter =
        new ExampleFilter(null, null, null, null, null, null, null, null, null, Set.of("a", "b"));
    var page = repository.findAll(ExampleSpecifications.matches(filter), PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.getContent())
        .extracting(ExampleEntity::getName)
        .containsExactlyInAnyOrder("multi", "single");
  }

  @Test
  void tagsContainsAny_blanksAreIgnoredAndAllBlanksDropsFilter() {
    persist("a", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of("red"));
    persist("b", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of("blue"));

    Set<String> blanksOnly = new HashSet<>();
    blanksOnly.add("");
    blanksOnly.add("   ");
    var allBlank =
        new ExampleFilter(null, null, null, null, null, null, null, null, null, blanksOnly);
    var allBlankPage =
        repository.findAll(ExampleSpecifications.matches(allBlank), Pageable.unpaged());
    assertThat(allBlankPage.getContent())
        .extracting(ExampleEntity::getName)
        .containsExactlyInAnyOrder("a", "b");

    Set<String> mixed = new HashSet<>();
    mixed.add(" ");
    mixed.add("red");
    var mixedFilter =
        new ExampleFilter(null, null, null, null, null, null, null, null, null, mixed);
    var mixedPage =
        repository.findAll(ExampleSpecifications.matches(mixedFilter), Pageable.unpaged());
    assertThat(mixedPage.getContent()).extracting(ExampleEntity::getName).containsExactly("a");
  }

  @Test
  void nameContains_treatsPercentAndUnderscoreAsLiterals() {
    persist("100%", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("1000", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("a_b", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("axb", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());

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
  void nullFilter_returnsAll() {
    persist("a", null, 1, new BigDecimal("1"), T0, ExampleStatus.ACTIVE, Set.of());
    persist("b", null, 1, new BigDecimal("1"), T0, ExampleStatus.DRAFT, Set.of());

    var spec = ExampleSpecifications.matches(null);
    var all = repository.findAll(spec);
    assertThat(all).hasSize(2);
  }
}
