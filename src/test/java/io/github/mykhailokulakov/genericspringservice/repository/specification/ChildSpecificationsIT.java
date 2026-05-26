package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.repository.ChildRepository;
import io.github.mykhailokulakov.genericspringservice.repository.ParentRepository;
import io.github.mykhailokulakov.genericspringservice.support.PersistenceTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

@PersistenceTest
@Import(DatabaseStateHelper.class)
class ChildSpecificationsIT {

  @Autowired private ChildRepository childRepository;
  @Autowired private ParentRepository parentRepository;
  @Autowired private DatabaseStateHelper db;

  @BeforeEach
  void clean() {
    db.truncateAll();
  }

  private ParentEntity persistParent(String label) {
    return parentRepository.saveAndFlush(ParentEntity.builder().label(label).build());
  }

  private ChildEntity persistChild(String value, ParentEntity parent) {
    return childRepository.saveAndFlush(ChildEntity.builder().value(value).parent(parent).build());
  }

  @Test
  void allNullParams_returnsAll() {
    var parent = persistParent("p");
    persistChild("a", parent);
    persistChild("b", parent);
    var result =
        childRepository.findAll(ChildSpecifications.matches(null, null, null), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void valueContains_matchesCaseInsensitively() {
    var parent = persistParent("p");
    persistChild("Alpha", parent);
    persistChild("Beta", parent);
    var result =
        childRepository.findAll(
            ChildSpecifications.matches(null, null, "ALPHA"), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getValue()).isEqualTo("Alpha");
  }

  @Test
  void valueContains_noMatch_returnsEmpty() {
    var parent = persistParent("p");
    persistChild("Alpha", parent);
    var result =
        childRepository.findAll(
            ChildSpecifications.matches(null, null, "Gamma"), Pageable.unpaged());
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void parentIdIn_returnsOnlyMatchingChildren() {
    var parent1 = persistParent("p1");
    var parent2 = persistParent("p2");
    persistChild("c1", parent1);
    persistChild("c2", parent2);
    var result =
        childRepository.findAll(
            ChildSpecifications.matches(null, List.of(parent1.getId()), null), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getValue()).isEqualTo("c1");
  }

  @Test
  void parentIdIn_noMatch_returnsEmpty() {
    var parent = persistParent("p");
    persistChild("c1", parent);
    var result =
        childRepository.findAll(
            ChildSpecifications.matches(null, List.of(UUID.randomUUID()), null),
            Pageable.unpaged());
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void idIn_filtersById() {
    var parent = persistParent("p");
    var c1 = persistChild("c1", parent);
    persistChild("c2", parent);
    var result =
        childRepository.findAll(
            ChildSpecifications.matches(List.of(c1.getId()), null, null), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getValue()).isEqualTo("c1");
  }
}
