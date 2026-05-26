package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.in;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.rangeBetween;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ExampleSpecifications {

  private ExampleSpecifications() {}

  public static Specification<ExampleEntity> matches(
      List<UUID> ids,
      String name,
      String description,
      Integer minQuantity,
      Integer maxQuantity,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Instant occurredFrom,
      Instant occurredTo,
      Set<ExampleStatus> statuses,
      Set<String> tags) {
    return allOfNonNull(
        in(ExampleEntity_.id, ids),
        containsIgnoreCase(ExampleEntity_.name, name),
        containsIgnoreCase(ExampleEntity_.description, description),
        rangeBetween(ExampleEntity_.quantity, minQuantity, maxQuantity),
        rangeBetween(ExampleEntity_.price, minPrice, maxPrice),
        rangeBetween(ExampleEntity_.occurredAt, occurredFrom, occurredTo),
        in(ExampleEntity_.status, statuses),
        hasAnyTag(tags));
  }

  private static Specification<ExampleEntity> hasAnyTag(Set<String> tags) {
    if (tags == null || tags.isEmpty()) return null;
    var specs =
        tags.stream()
            .filter(StringUtils::hasText)
            .map(
                tag ->
                    (Specification<ExampleEntity>)
                        (root, q, cb) -> cb.isMember(tag, root.get(ExampleEntity_.tags)))
            .toList();
    return specs.isEmpty() ? null : Specification.anyOf(specs);
  }
}
