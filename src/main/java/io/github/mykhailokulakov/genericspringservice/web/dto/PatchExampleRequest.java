package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record PatchExampleRequest(
    String name,
    String description,
    Integer quantity,
    BigDecimal price,
    Instant occurredAt,
    ExampleStatus status,
    Set<String> tags) {}
