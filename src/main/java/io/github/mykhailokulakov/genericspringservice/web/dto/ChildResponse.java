package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A Child resource.")
public record ChildResponse(
    @Schema(description = "Resource identifier.") UUID id,
    @Schema(description = "Value.") String value,
    @Schema(description = "Parent resource identifier.") UUID parentId,
    @Schema(description = "Creation timestamp (UTC).") Instant createdAt,
    @Schema(description = "Last-modification timestamp (UTC).") Instant updatedAt,
    @Schema(description = "Optimistic-locking version.") Long version) {}
