package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.RightFilter;
import org.springframework.data.jpa.domain.Specification;

public final class RightSpecifications {

  private RightSpecifications() {}

  public static Specification<RightEntity> matches(RightFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    return allOfNonNull(containsIgnoreCase(RightEntity_.name, f.name()));
  }
}
