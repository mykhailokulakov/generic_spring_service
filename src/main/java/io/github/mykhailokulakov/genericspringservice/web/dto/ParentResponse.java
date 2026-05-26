package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A Parent resource.")
public record ParentResponse(
    @Schema(description = "Resource identifier.") UUID id,
    @Schema(description = "Label.") String label,
    @Schema(description = "Creation timestamp (UTC).") Instant createdAt,
    @Schema(description = "Last-modification timestamp (UTC).") Instant updatedAt,
    @Schema(description = "Optimistic-locking version.") Long version) {}
