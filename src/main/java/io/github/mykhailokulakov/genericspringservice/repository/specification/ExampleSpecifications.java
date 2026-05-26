package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.allOfNonNull;
import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ExampleSpecifications {

  private ExampleSpecifications() {}

  public static Specification<ExampleEntity> matches(ExampleFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    return allOfNonNull(
        containsIgnoreCase(ExampleEntity_.name, f.name()),
        containsIgnoreCase(ExampleEntity_.description, f.description()),
        rangeBetween(ExampleEntity_.quantity, f.minQuantity(), f.maxQuantity()),
        rangeBetween(ExampleEntity_.price, f.minPrice(), f.maxPrice()),
        rangeBetween(ExampleEntity_.occurredAt, f.occurredFrom(), f.occurredTo()),
        statusIn(f.statuses()),
        hasAnyTag(f.tags()));
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
