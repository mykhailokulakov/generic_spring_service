package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

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
        idIn(ids), exampleIdIn(exampleIds), containsIgnoreCase(OwnerEntity_.handle, handle));
  }

  private static Specification<OwnerEntity> idIn(List<UUID> ids) {
    if (ids == null || ids.isEmpty()) return null;
    return (root, q, cb) -> root.get(OwnerEntity_.id).in(ids);
  }

  private static Specification<OwnerEntity> exampleIdIn(List<UUID> exampleIds) {
    if (exampleIds == null || exampleIds.isEmpty()) return null;
    return (root, q, cb) -> root.get(OwnerEntity_.example).get(ExampleEntity_.id).in(exampleIds);
  }
}
