package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity_;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ParentSpecifications {

  private ParentSpecifications() {}

  public static Specification<ParentEntity> matches(List<UUID> ids, String label) {
    return allOfNonNull(idIn(ids), containsIgnoreCase(ParentEntity_.label, label));
  }

  private static Specification<ParentEntity> idIn(List<UUID> ids) {
    if (ids == null || ids.isEmpty()) return null;
    return (root, q, cb) -> root.get(ParentEntity_.id).in(ids);
  }
}
