package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.fkIn;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.in;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity_;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class OwnerSpecifications {

  private OwnerSpecifications() {}

  public static Specification<OwnerEntity> matches(
      List<UUID> ids, List<UUID> exampleIds, String handle) {
    return allOfNonNull(
        in(OwnerEntity_.id, ids),
        fkIn(OwnerEntity_.example, ExampleEntity_.id, exampleIds),
        containsIgnoreCase(OwnerEntity_.handle, handle));
  }
}
