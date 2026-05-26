package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A Right resource.")
public record RightResponse(
    @Schema(description = "Resource identifier.") UUID id,
    @Schema(description = "Human-readable name.") String name,
    @Schema(description = "Creation timestamp (UTC).") Instant createdAt,
    @Schema(description = "Last-modification timestamp (UTC).") Instant updatedAt,
    @Schema(description = "Optimistic-locking version. Use as the If-Match header on PUT/PATCH.")
        Long version) {}
