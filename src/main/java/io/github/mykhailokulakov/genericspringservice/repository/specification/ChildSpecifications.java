package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity_;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ChildSpecifications {

  private ChildSpecifications() {}

  public static Specification<ChildEntity> matches(
      List<UUID> ids, List<UUID> parentIds, String value) {
    return allOfNonNull(
        idIn(ids), parentIdIn(parentIds), containsIgnoreCase(ChildEntity_.value, value));
  }

  private static Specification<ChildEntity> idIn(List<UUID> ids) {
    if (ids == null || ids.isEmpty()) return null;
    return (root, q, cb) -> root.get(ChildEntity_.id).in(ids);
  }

  private static Specification<ChildEntity> parentIdIn(List<UUID> parentIds) {
    if (parentIds == null || parentIds.isEmpty()) return null;
    return (root, q, cb) -> root.get(ChildEntity_.parent).get(ParentEntity_.id).in(parentIds);
  }
}
