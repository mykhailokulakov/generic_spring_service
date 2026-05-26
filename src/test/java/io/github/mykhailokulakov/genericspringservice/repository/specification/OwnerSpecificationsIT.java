package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerFilter;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.repository.OwnerRepository;
import io.github.mykhailokulakov.genericspringservice.support.PersistenceTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

@PersistenceTest
@Import(DatabaseStateHelper.class)
class OwnerSpecificationsIT {

  @Autowired private OwnerRepository ownerRepository;
  @Autowired private ExampleRepository exampleRepository;
  @Autowired private DatabaseStateHelper db;

  @BeforeEach
  void clean() {
    db.truncateAll();
  }

  private ExampleEntity persistExample(String name) {
    return exampleRepository.saveAndFlush(
        ExampleEntity.builder()
            .name(name)
            .status(ExampleStatus.ACTIVE)
            .price(BigDecimal.ONE)
            .build());
  }

  private OwnerEntity persistOwner(String handle, ExampleEntity example) {
    return ownerRepository.saveAndFlush(
        OwnerEntity.builder().handle(handle).example(example).build());
  }

  @Test
  void nullFilter_returnsAll() {
    var ex = persistExample("e");
    persistOwner("a", ex);
    persistOwner("b", null);
    var result = ownerRepository.findAll(OwnerSpecifications.matches(null), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void emptyFilter_returnsAll() {
    var ex = persistExample("e");
    persistOwner("a", ex);
    persistOwner("b", null);
    var result =
        ownerRepository.findAll(
            OwnerSpecifications.matches(OwnerFilter.empty()), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void handleContains_matchesCaseInsensitively() {
    persistOwner("Alpha", null);
    persistOwner("Beta", null);
    var result =
        ownerRepository.findAll(
            OwnerSpecifications.matches(new OwnerFilter("ALPHA", null)), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getHandle()).isEqualTo("Alpha");
  }

  @Test
  void handleContains_noMatch_returnsEmpty() {
    persistOwner("Alpha", null);
    var result =
        ownerRepository.findAll(
            OwnerSpecifications.matches(new OwnerFilter("Gamma", null)), Pageable.unpaged());
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void exampleIdEquals_returnsOnlyMatchingOwners() {
    var ex1 = persistExample("e1");
    var ex2 = persistExample("e2");
    persistOwner("o1", ex1);
    persistOwner("o2", ex2);
    var result =
        ownerRepository.findAll(
            OwnerSpecifications.matches(new OwnerFilter(null, ex1.getId())), Pageable.unpaged());
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getHandle()).isEqualTo("o1");
  }

  @Test
  void exampleIdEquals_noMatch_returnsEmpty() {
    var ex = persistExample("e");
    persistOwner("o1", ex);
    var result =
        ownerRepository.findAll(
            OwnerSpecifications.matches(new OwnerFilter(null, UUID.randomUUID())),
            Pageable.unpaged());
    assertThat(result.getContent()).isEmpty();
  }
}
