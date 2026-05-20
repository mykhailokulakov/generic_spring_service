package io.github.mykhailokulakov.genericspringservice.repository.specification;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.web.dto.ExampleFilter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ExampleSpecifications {

  private ExampleSpecifications() {}

  public static Specification<ExampleEntity> matches(ExampleFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    List<Specification<ExampleEntity>> parts = new ArrayList<>();
    addIfPresent(parts, nameContains(f.name()));
    addIfPresent(parts, descriptionContains(f.description()));
    addIfPresent(parts, quantityBetween(f.minQuantity(), f.maxQuantity()));
    addIfPresent(parts, priceBetween(f.minPrice(), f.maxPrice()));
    addIfPresent(parts, occurredBetween(f.occurredFrom(), f.occurredTo()));
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

  private static Specification<ExampleEntity> nameContains(String value) {
    if (!StringUtils.hasText(value)) return null;
    String pattern = "%" + escapeLikePattern(value).toLowerCase(Locale.ROOT) + "%";
    return (root, q, cb) -> cb.like(cb.lower(root.get(ExampleEntity_.name)), pattern, LIKE_ESCAPE);
  }

  private static Specification<ExampleEntity> descriptionContains(String value) {
    if (!StringUtils.hasText(value)) return null;
    String pattern = "%" + escapeLikePattern(value).toLowerCase(Locale.ROOT) + "%";
    return (root, q, cb) ->
        cb.like(cb.lower(root.get(ExampleEntity_.description)), pattern, LIKE_ESCAPE);
  }

  // Escape character used in LIKE patterns so user-supplied `%` / `_` literals
  // don't act as wildcards.
  private static final char LIKE_ESCAPE = '\\';

  private static String escapeLikePattern(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  private static Specification<ExampleEntity> quantityBetween(Integer min, Integer max) {
    if (min == null && max == null) return null;
    return (root, q, cb) -> {
      if (min != null && max != null) {
        return cb.between(root.get(ExampleEntity_.quantity), min, max);
      }
      if (min != null) {
        return cb.greaterThanOrEqualTo(root.get(ExampleEntity_.quantity), min);
      }
      return cb.lessThanOrEqualTo(root.get(ExampleEntity_.quantity), max);
    };
  }

  private static Specification<ExampleEntity> priceBetween(BigDecimal min, BigDecimal max) {
    if (min == null && max == null) return null;
    return (root, q, cb) -> {
      if (min != null && max != null) {
        return cb.between(root.get(ExampleEntity_.price), min, max);
      }
      if (min != null) {
        return cb.greaterThanOrEqualTo(root.get(ExampleEntity_.price), min);
      }
      return cb.lessThanOrEqualTo(root.get(ExampleEntity_.price), max);
    };
  }

  private static Specification<ExampleEntity> occurredBetween(Instant from, Instant to) {
    if (from == null && to == null) return null;
    return (root, q, cb) -> {
      if (from != null && to != null) {
        return cb.between(root.get(ExampleEntity_.occurredAt), from, to);
      }
      if (from != null) {
        return cb.greaterThanOrEqualTo(root.get(ExampleEntity_.occurredAt), from);
      }
      return cb.lessThanOrEqualTo(root.get(ExampleEntity_.occurredAt), to);
    };
  }

  private static Specification<ExampleEntity> statusIn(Set<ExampleStatus> statuses) {
    if (statuses == null || statuses.isEmpty()) return null;
    return (root, q, cb) -> root.get(ExampleEntity_.status).in(statuses);
  }

  // Uses cb.isMember per tag OR'd together rather than a join. A join on the
  // @ElementCollection multiplies rows for entities that match more than one
  // requested tag — DISTINCT fixes the page query but the separate count query
  // would still report an inflated total. cb.isMember translates to an EXISTS-
  // style check, leaving both data and count queries one-row-per-entity.
  //
  // Null/blank tags are filtered out so they don't reach cb.isMember. If the
  // request contained only blanks, the whole filter is dropped (returns null)
  // rather than producing a predicate that matches nothing.
  private static Specification<ExampleEntity> hasAnyTag(Set<String> tags) {
    if (tags == null || tags.isEmpty()) return null;
    List<Specification<ExampleEntity>> specs =
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
