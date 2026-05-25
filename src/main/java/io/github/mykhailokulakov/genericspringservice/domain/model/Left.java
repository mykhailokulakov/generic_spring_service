package io.github.mykhailokulakov.genericspringservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record Left(UUID id, String code, Instant createdAt, Instant updatedAt, Long version)
    implements DomainModel {}
