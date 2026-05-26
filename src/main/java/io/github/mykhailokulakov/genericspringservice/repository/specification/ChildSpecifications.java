package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.ChildFilter;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ChildSpecifications {

  private ChildSpecifications() {}

  public static Specification<ChildEntity> matches(ChildFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    return allOfNonNull(
        containsIgnoreCase(ChildEntity_.value, f.value()), parentIdEquals(f.parentId()));
  }

  private static Specification<ChildEntity> parentIdEquals(UUID parentId) {
    if (parentId == null) return null;
    return (root, q, cb) -> cb.equal(root.get(ChildEntity_.parent).get(ParentEntity_.id), parentId);
  }
}
