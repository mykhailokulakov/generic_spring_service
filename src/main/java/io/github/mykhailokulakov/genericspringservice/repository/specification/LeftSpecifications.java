package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity_;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class LeftSpecifications {

  private LeftSpecifications() {}

  public static Specification<LeftEntity> matches(List<UUID> ids, String code) {
    return allOfNonNull(idIn(ids), containsIgnoreCase(LeftEntity_.code, code));
  }

  private static Specification<LeftEntity> idIn(List<UUID> ids) {
    if (ids == null || ids.isEmpty()) return null;
    return (root, q, cb) -> root.get(LeftEntity_.id).in(ids);
  }
}
