package io.github.mykhailokulakov.genericspringservice.repository.query;

import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.containsIgnoreCase;
import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.in;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.github.mykhailokulakov.genericspringservice.domain.entity.QOwnerEntity;
import java.util.List;
import java.util.UUID;

public final class OwnerQueries {

  private OwnerQueries() {}

  public static Predicate matches(List<UUID> ids, List<UUID> exampleIds, String handle) {
    var q = QOwnerEntity.ownerEntity;
    return new BooleanBuilder()
        .and(in(q.id, ids))
        .and(in(q.example.id, exampleIds))
        .and(containsIgnoreCase(q.handle, handle));
  }
}
