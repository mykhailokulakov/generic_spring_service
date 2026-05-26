package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.ChildFilter;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ChildSpecifications {

  private ChildSpecifications() {}

  public static Specification<ChildEntity> matches(ChildFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    var parts = new ArrayList<Specification<ChildEntity>>();
    var valueSpec = containsIgnoreCase(ChildEntity_.value, f.value());
    if (valueSpec != null) {
      parts.add(valueSpec);
    }
    var parentSpec = parentIdEquals(f.parentId());
    if (parentSpec != null) {
      parts.add(parentSpec);
    }
    if (parts.isEmpty()) {
      return Specification.unrestricted();
    }
    return Specification.allOf(parts);
  }

  private static Specification<ChildEntity> parentIdEquals(UUID parentId) {
    if (parentId == null) return null;
    return (root, q, cb) -> cb.equal(root.get(ChildEntity_.parent).get(ParentEntity_.id), parentId);
  }
}
