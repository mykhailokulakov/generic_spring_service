package io.github.mykhailokulakov.genericspringservice.repository.specification;

import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class SpecificationUtils {

  private SpecificationUtils() {}

  private static final char LIKE_ESCAPE = '\\';

  @SafeVarargs
  public static <E> Specification<E> allOfNonNull(Specification<E>... specs) {
    var nonNull = Arrays.stream(specs).filter(Objects::nonNull).toList();
    return nonNull.isEmpty() ? Specification.unrestricted() : Specification.allOf(nonNull);
  }

  public static <E, V> Specification<E> in(
      SingularAttribute<? super E, V> attr, Collection<? extends V> values) {
    if (values == null || values.isEmpty()) return null;
    return (root, q, cb) -> root.get(attr).in(values);
  }

  public static <E, J, V> Specification<E> fkIn(
      SingularAttribute<? super E, J> joinAttr,
      SingularAttribute<? super J, V> idAttr,
      Collection<? extends V> values) {
    if (values == null || values.isEmpty()) return null;
    return (root, q, cb) -> root.get(joinAttr).get(idAttr).in(values);
  }

  public static <E> Specification<E> containsIgnoreCase(
      SingularAttribute<? super E, String> attr, String value) {
    if (!StringUtils.hasText(value)) return null;
    var pattern = "%" + escapeLikePattern(value).toLowerCase(Locale.ROOT) + "%";
    return (root, q, cb) -> cb.like(cb.lower(root.get(attr)), pattern, LIKE_ESCAPE);
  }

  public static <E, T extends Comparable<? super T>> Specification<E> rangeBetween(
      SingularAttribute<? super E, T> attr, T min, T max) {
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

  static String escapeLikePattern(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
