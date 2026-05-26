package io.github.mykhailokulakov.genericspringservice.repository.specification;

import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Arrays;
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

  public static <E> Specification<E> containsIgnoreCase(
      SingularAttribute<E, String> attr, String value) {
    if (!StringUtils.hasText(value)) return null;
    var pattern = "%" + escapeLikePattern(value).toLowerCase(Locale.ROOT) + "%";
    return (root, q, cb) -> cb.like(cb.lower(root.get(attr)), pattern, LIKE_ESCAPE);
  }

  static String escapeLikePattern(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
