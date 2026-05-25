package io.github.mykhailokulakov.genericspringservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record Parent(UUID id, String label, Instant createdAt, Instant updatedAt, Long version)
    implements DomainModel {}
