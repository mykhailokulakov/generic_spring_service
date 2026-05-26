package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.repository.ParentRepository;
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
class ParentSpecificationsIT {

  @Autowired private ParentRepository repository;
  @Autowired private DatabaseStateHelper db;

  @BeforeEach
  void clean() {
    db.truncateAll();
  }

  private ParentEntity persist(String label) {
    return repository.saveAndFlush(ParentEntity.builder().label(label).build());
  }

  @Test
  void allNullParams_returnsAll() {
    persist("a");
    persist("b");
    var result = repository.findAll(ParentSpecifications.matches(null, null), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void labelContains_matchesCaseInsensitively() {
    persist("Alpha");
    persist("Beta");
    var result =
        repository.findAll(ParentSpecifications.matches(null, "ALPHA"), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getLabel()).isEqualTo("Alpha");
  }

  @Test
  void labelContains_noMatch_returnsEmpty() {
    persist("Alpha");
    var result =
        repository.findAll(ParentSpecifications.matches(null, "Gamma"), Pageable.unpaged());
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void idIn_filtersById() {
    var a = persist("a");
    persist("b");
    var result =
        repository.findAll(
            ParentSpecifications.matches(List.of(a.getId()), null), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getLabel()).isEqualTo("a");
  }
}
