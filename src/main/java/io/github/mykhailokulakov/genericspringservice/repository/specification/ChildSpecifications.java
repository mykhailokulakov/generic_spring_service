package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.fkIn;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.in;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity_;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ChildSpecifications {

  private ChildSpecifications() {}

  public static Specification<ChildEntity> matches(
      List<UUID> ids, List<UUID> parentIds, String value) {
    return allOfNonNull(
        in(ChildEntity_.id, ids),
        fkIn(ChildEntity_.parent, ParentEntity_.id, parentIds),
        containsIgnoreCase(ChildEntity_.value, value));
  }
}
