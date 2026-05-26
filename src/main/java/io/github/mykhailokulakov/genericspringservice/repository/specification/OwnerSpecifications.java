package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerFilter;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class OwnerSpecifications {

  private OwnerSpecifications() {}

  public static Specification<OwnerEntity> matches(OwnerFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    return allOfNonNull(
        containsIgnoreCase(OwnerEntity_.handle, f.handle()), exampleIdEquals(f.exampleId()));
  }

  private static Specification<OwnerEntity> exampleIdEquals(UUID exampleId) {
    if (exampleId == null) return null;
    return (root, q, cb) ->
        cb.equal(root.get(OwnerEntity_.example).get(ExampleEntity_.id), exampleId);
  }
}
