package io.github.mykhailokulakov.genericspringservice.repository.query;

import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.anyMember;
import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.containsIgnoreCase;
import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.in;
import static io.github.mykhailokulakov.genericspringservice.repository.query.QueryPredicates.range;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.github.mykhailokulakov.genericspringservice.domain.entity.QExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ExampleQueries {

  private ExampleQueries() {}

  public static Predicate matches(
      List<UUID> ids,
      String name,
      String description,
      Integer minQuantity,
      Integer maxQuantity,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Instant occurredFrom,
      Instant occurredTo,
      Set<ExampleStatus> statuses,
      Set<String> tags) {
    var q = QExampleEntity.exampleEntity;
    return new BooleanBuilder()
        .and(in(q.id, ids))
        .and(containsIgnoreCase(q.name, name))
        .and(containsIgnoreCase(q.description, description))
        .and(range(q.quantity, minQuantity, maxQuantity))
        .and(range(q.price, minPrice, maxPrice))
        .and(range(q.occurredAt, occurredFrom, occurredTo))
        .and(in(q.status, statuses))
        .and(anyMember(q.tags, tags));
  }
}
