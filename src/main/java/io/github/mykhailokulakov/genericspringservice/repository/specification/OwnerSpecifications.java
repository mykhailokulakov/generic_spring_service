package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerFilter;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class OwnerSpecifications {

  private OwnerSpecifications() {}

  public static Specification<OwnerEntity> matches(OwnerFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    var parts = new ArrayList<Specification<OwnerEntity>>();
    var handleSpec = containsIgnoreCase(OwnerEntity_.handle, f.handle());
    if (handleSpec != null) parts.add(handleSpec);
    var exampleSpec = exampleIdEquals(f.exampleId());
    if (exampleSpec != null) parts.add(exampleSpec);
    if (parts.isEmpty()) {
      return Specification.unrestricted();
    }
    return Specification.allOf(parts);
  }

  private static Specification<OwnerEntity> exampleIdEquals(UUID exampleId) {
    if (exampleId == null) return null;
    return (root, q, cb) ->
        cb.equal(root.get(OwnerEntity_.example).get(ExampleEntity_.id), exampleId);
  }
}
