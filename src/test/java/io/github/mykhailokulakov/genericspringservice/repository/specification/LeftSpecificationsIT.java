package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.repository.LeftRepository;
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
class LeftSpecificationsIT {

  @Autowired private LeftRepository repository;
  @Autowired private DatabaseStateHelper db;

  @BeforeEach
  void clean() {
    db.truncateAll();
  }

  private LeftEntity persist(String code) {
    return repository.saveAndFlush(LeftEntity.builder().code(code).build());
  }

  @Test
  void allNullParams_returnsAll() {
    persist("a");
    persist("b");
    var result = repository.findAll(LeftSpecifications.matches(null, null), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void codeContains_matchesCaseInsensitively() {
    persist("Alpha");
    persist("Beta");
    var result = repository.findAll(LeftSpecifications.matches(null, "ALPHA"), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getCode()).isEqualTo("Alpha");
  }

  @Test
  void codeContains_noMatch_returnsEmpty() {
    persist("Alpha");
    var result = repository.findAll(LeftSpecifications.matches(null, "Gamma"), Pageable.unpaged());
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void idIn_filtersById() {
    var a = persist("a");
    persist("b");
    var result =
        repository.findAll(
            LeftSpecifications.matches(List.of(a.getId()), null), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getCode()).isEqualTo("a");
  }
}
