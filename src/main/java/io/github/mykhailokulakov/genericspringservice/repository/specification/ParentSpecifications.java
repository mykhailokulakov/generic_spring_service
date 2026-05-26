package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.ParentFilter;
import org.springframework.data.jpa.domain.Specification;

public final class ParentSpecifications {

  private ParentSpecifications() {}

  public static Specification<ParentEntity> matches(ParentFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    return allOfNonNull(containsIgnoreCase(ParentEntity_.label, f.label()));
  }
}
