package io.github.mykhailokulakov.genericspringservice.repository.query;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SetPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Collection;
import org.springframework.util.StringUtils;

public final class QueryPredicates {

  private QueryPredicates() {}

  public static <T> Predicate in(SimpleExpression<T> path, Collection<? extends T> values) {
    return (values == null || values.isEmpty()) ? null : path.in(values);
  }

  public static Predicate containsIgnoreCase(StringPath path, String value) {
    return StringUtils.hasText(value) ? path.containsIgnoreCase(value) : null;
  }

  public static <T extends Number & Comparable<T>> Predicate range(
      NumberExpression<T> path, T min, T max) {
    if (min != null && max != null) {
      return path.between(min, max);
    }
    if (min != null) {
      return path.goe(min);
    }
    if (max != null) {
      return path.loe(max);
    }
    return null;
  }

  public static <T extends Comparable<?>> Predicate range(
      DateTimeExpression<T> path, T min, T max) {
    if (min != null && max != null) {
      return path.between(min, max);
    }
    if (min != null) {
      return path.goe(min);
    }
    if (max != null) {
      return path.loe(max);
    }
    return null;
  }

  public static <T extends Comparable<?>> Predicate range(
      ComparableExpression<T> path, T min, T max) {
    if (min != null && max != null) {
      return path.between(min, max);
    }
    if (min != null) {
      return path.goe(min);
    }
    if (max != null) {
      return path.loe(max);
    }
    return null;
  }

  public static <T> Predicate anyMember(SetPath<T, ?> path, Collection<? extends T> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    var matches =
        values.stream()
            .filter(v -> v != null && (!(v instanceof String s) || StringUtils.hasText(s)))
            .map(path::contains)
            .toArray(Predicate[]::new);
    return matches.length == 0 ? null : ExpressionUtils.anyOf(matches);
  }
}
