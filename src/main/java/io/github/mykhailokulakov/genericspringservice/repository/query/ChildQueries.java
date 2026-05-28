package io.github.mykhailokulakov.genericspringservice.repository.query;

import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.containsIgnoreCase;
import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.in;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.github.mykhailokulakov.genericspringservice.domain.entity.QChildEntity;
import java.util.List;
import java.util.UUID;

public final class ChildQueries {

  private ChildQueries() {}

  public static Predicate matches(List<UUID> ids, List<UUID> parentIds, String value) {
    var q = QChildEntity.childEntity;
    return new BooleanBuilder()
        .and(in(q.id, ids))
        .and(in(q.parent.id, parentIds))
        .and(containsIgnoreCase(q.value, value));
  }
}
