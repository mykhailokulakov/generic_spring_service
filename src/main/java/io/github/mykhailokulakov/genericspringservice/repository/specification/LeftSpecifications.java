package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.LeftFilter;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;

public final class LeftSpecifications {

  private LeftSpecifications() {}

  public static Specification<LeftEntity> matches(LeftFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    var parts = new ArrayList<Specification<LeftEntity>>();
    var codeSpec = containsIgnoreCase(LeftEntity_.code, f.code());
    if (codeSpec != null) {
      parts.add(codeSpec);
    }
    if (parts.isEmpty()) {
      return Specification.unrestricted();
    }
    return Specification.allOf(parts);
  }
}
