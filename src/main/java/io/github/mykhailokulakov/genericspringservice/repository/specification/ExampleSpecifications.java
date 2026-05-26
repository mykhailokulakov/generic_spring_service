package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import jakarta.persistence.metamodel.SingularAttribute;
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
        idIn(ids),
        containsIgnoreCase(ExampleEntity_.name, name),
        containsIgnoreCase(ExampleEntity_.description, description),
        rangeBetween(ExampleEntity_.quantity, minQuantity, maxQuantity),
        rangeBetween(ExampleEntity_.price, minPrice, maxPrice),
        rangeBetween(ExampleEntity_.occurredAt, occurredFrom, occurredTo),
        statusIn(statuses),
        hasAnyTag(tags));
  }

  private static Specification<ExampleEntity> idIn(List<UUID> ids) {
    if (ids == null || ids.isEmpty()) return null;
    return (root, q, cb) -> root.get(ExampleEntity_.id).in(ids);
  }

  private static <T extends Comparable<? super T>> Specification<ExampleEntity> rangeBetween(
      SingularAttribute<ExampleEntity, T> attr, T min, T max) {
    if (min == null && max == null) return null;
    return (root, q, cb) -> {
      if (min != null && max != null) {
        return cb.between(root.get(attr), min, max);
      }
      if (min != null) {
        return cb.greaterThanOrEqualTo(root.get(attr), min);
      }
      return cb.lessThanOrEqualTo(root.get(attr), max);
    };
  }

  private static Specification<ExampleEntity> statusIn(Set<ExampleStatus> statuses) {
    if (statuses == null || statuses.isEmpty()) return null;
    return (root, q, cb) -> root.get(ExampleEntity_.status).in(statuses);
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
