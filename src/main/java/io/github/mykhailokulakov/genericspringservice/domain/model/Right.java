package io.github.mykhailokulakov.genericspringservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record Right(UUID id, String name, Instant createdAt, Instant updatedAt, Long version)
    implements DomainModel {}
