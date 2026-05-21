package io.github.mykhailokulakov.genericspringservice.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record ExampleFilter(
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

  public static ExampleFilter empty() {
    return new ExampleFilter(null, null, null, null, null, null, null, null, null, null);
  }
}
