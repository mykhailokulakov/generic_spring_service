package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "An Owner resource.")
public record OwnerResponse(
    @Schema(description = "Resource identifier.") UUID id,
    @Schema(description = "Handle.") String handle,
    @Schema(description = "Associated example identifier.") UUID exampleId,
    @Schema(description = "Creation timestamp (UTC).") Instant createdAt,
    @Schema(description = "Last-modification timestamp (UTC).") Instant updatedAt,
    @Schema(description = "Optimistic-locking version.") Long version) {}
