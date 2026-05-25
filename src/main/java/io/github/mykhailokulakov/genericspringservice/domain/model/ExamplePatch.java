package io.github.mykhailokulakov.genericspringservice.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import lombok.Builder;

@Builder(toBuilder = true)
public record ExamplePatch(
    String name,
    String description,
    Integer quantity,
    BigDecimal price,
    Instant occurredAt,
    ExampleStatus status,
    Set<String> tags) {}
