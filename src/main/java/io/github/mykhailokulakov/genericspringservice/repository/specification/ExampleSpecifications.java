package io.github.mykhailokulakov.genericspringservice.repository.specification;

import static io.github.mykhailokulakov.genericspringservice.repository.specification.SpecificationUtils.containsIgnoreCase;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ExampleSpecifications {

  private ExampleSpecifications() {}

  public static Specification<ExampleEntity> matches(ExampleFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    var parts = new ArrayList<Specification<ExampleEntity>>();
    addIfPresent(parts, containsIgnoreCase(ExampleEntity_.name, f.name()));
    addIfPresent(parts, containsIgnoreCase(ExampleEntity_.description, f.description()));
    addIfPresent(parts, rangeBetween(ExampleEntity_.quantity, f.minQuantity(), f.maxQuantity()));
    addIfPresent(parts, rangeBetween(ExampleEntity_.price, f.minPrice(), f.maxPrice()));
    addIfPresent(parts, rangeBetween(ExampleEntity_.occurredAt, f.occurredFrom(), f.occurredTo()));
    addIfPresent(parts, statusIn(f.statuses()));
    addIfPresent(parts, hasAnyTag(f.tags()));
    if (parts.isEmpty()) {
      return Specification.unrestricted();
    }
    return Specification.allOf(parts);
  }

  private static void addIfPresent(
      List<Specification<ExampleEntity>> parts, Specification<ExampleEntity> spec) {
    if (spec != null) {
      parts.add(spec);
    }
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
