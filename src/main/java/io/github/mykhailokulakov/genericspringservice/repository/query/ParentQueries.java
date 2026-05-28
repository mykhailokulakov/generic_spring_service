package io.github.mykhailokulakov.genericspringservice.repository.query;

import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.containsIgnoreCase;
import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.in;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.github.mykhailokulakov.genericspringservice.domain.entity.QParentEntity;
import java.util.List;
import java.util.UUID;

public final class ParentQueries {

  private ParentQueries() {}

  public static Predicate matches(List<UUID> ids, String label) {
    var q = QParentEntity.parentEntity;
    return new BooleanBuilder().and(in(q.id, ids)).and(containsIgnoreCase(q.label, label));
  }
}
