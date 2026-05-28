package io.github.mykhailokulakov.genericspringservice.repository.query;

import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.containsIgnoreCase;
import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.in;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.github.mykhailokulakov.genericspringservice.domain.entity.QLeftEntity;
import java.util.List;
import java.util.UUID;

public final class LeftQueries {

  private LeftQueries() {}

  public static Predicate matches(List<UUID> ids, String code) {
    var q = QLeftEntity.leftEntity;
    return new BooleanBuilder().and(in(q.id, ids)).and(containsIgnoreCase(q.code, code));
  }
}
