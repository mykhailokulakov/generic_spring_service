package io.github.mykhailokulakov.genericspringservice.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record Example(
    UUID id,
    String name,
    String description,
    Integer quantity,
    BigDecimal price,
    Instant occurredAt,
    ExampleStatus status,
    Set<String> tags,
    Instant createdAt,
    Instant updatedAt,
    Long version)
    implements DomainModel {}
