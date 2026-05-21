package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "An Example resource.")
public record ExampleResponse(
    @Schema(description = "Resource identifier.") UUID id,
    @Schema(description = "Human-readable name.") String name,
    @Schema(description = "Free-form description.") String description,
    @Schema(description = "Quantity on hand.") Integer quantity,
    @Schema(description = "Unit price.") BigDecimal price,
    @Schema(description = "Business event timestamp (UTC).") Instant occurredAt,
    @Schema(description = "Lifecycle status.") ExampleStatus status,
    @Schema(description = "Free-form tags.") Set<String> tags,
    @Schema(description = "Creation timestamp (UTC).") Instant createdAt,
    @Schema(description = "Last-modification timestamp (UTC).") Instant updatedAt,
    @Schema(description = "Optimistic-locking version. Use as the If-Match header on PUT/PATCH.")
        Long version) {}
