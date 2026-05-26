package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.repository.RightRepository;
import io.github.mykhailokulakov.genericspringservice.support.PersistenceTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import java.util.List;
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
  void allNullParams_returnsAll() {
    persist("a");
    persist("b");
    var result = repository.findAll(RightSpecifications.matches(null, null), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void nameContains_matchesCaseInsensitively() {
    persist("Alpha");
    persist("Beta");
    var result = repository.findAll(RightSpecifications.matches(null, "ALPHA"), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getName()).isEqualTo("Alpha");
  }

  @Test
  void nameContains_noMatch_returnsEmpty() {
    persist("Alpha");
    var result = repository.findAll(RightSpecifications.matches(null, "Gamma"), Pageable.unpaged());
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void idIn_filtersById() {
    var a = persist("a");
    persist("b");
    var result =
        repository.findAll(
            RightSpecifications.matches(List.of(a.getId()), null), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getName()).isEqualTo("a");
  }
}
