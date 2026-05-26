package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.ParentFilter;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;

public final class ParentSpecifications {

  private ParentSpecifications() {}

  public static Specification<ParentEntity> matches(ParentFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    var parts = new ArrayList<Specification<ParentEntity>>();
    var labelSpec = containsIgnoreCase(ParentEntity_.label, f.label());
    if (labelSpec != null) {
      parts.add(labelSpec);
    }
    if (parts.isEmpty()) {
      return Specification.unrestricted();
    }
    return Specification.allOf(parts);
  }
}
