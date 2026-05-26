package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.RightFilter;
import io.github.mykhailokulakov.genericspringservice.repository.RightRepository;
import io.github.mykhailokulakov.genericspringservice.support.PersistenceTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

@PersistenceTest
@Import(DatabaseStateHelper.class)
class RightSpecificationsIT {

  @Autowired private RightRepository repository;
  @Autowired private DatabaseStateHelper db;

  @BeforeEach
  void clean() {
    db.truncateAll();
  }

  private RightEntity persist(String name) {
    return repository.saveAndFlush(RightEntity.builder().name(name).build());
  }

  @Test
  void nullFilter_returnsAll() {
    persist("a");
    persist("b");
    var result = repository.findAll(RightSpecifications.matches(null), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void emptyFilter_returnsAll() {
    persist("a");
    persist("b");
    var result =
        repository.findAll(RightSpecifications.matches(RightFilter.empty()), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void nameContains_matchesCaseInsensitively() {
    persist("Alpha");
    persist("Beta");
    var result =
        repository.findAll(
            RightSpecifications.matches(new RightFilter("ALPHA")), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getName()).isEqualTo("Alpha");
  }

  @Test
  void nameContains_noMatch_returnsEmpty() {
    persist("Alpha");
    var result =
        repository.findAll(
            RightSpecifications.matches(new RightFilter("Gamma")), Pageable.unpaged());
    assertThat(result.getContent()).isEmpty();
  }
}
