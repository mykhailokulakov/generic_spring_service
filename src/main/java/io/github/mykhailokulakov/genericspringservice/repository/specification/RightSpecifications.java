package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.in;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity_;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class RightSpecifications {

  private RightSpecifications() {}

  public static Specification<RightEntity> matches(List<UUID> ids, String name) {
    return allOfNonNull(in(RightEntity_.id, ids), containsIgnoreCase(RightEntity_.name, name));
  }
}
