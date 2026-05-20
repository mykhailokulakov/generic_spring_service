package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.github.mykhailokulakov.genericspringservice.common.validation.NullOrNotBlank;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Schema(
    description =
        "Partial-update payload for an Example. Patch semantics: a null field means \"leave"
            + " unchanged\". There is no way to clear a field via PATCH (see DESIGN.md section"
            + " 3.4).")
public record PatchExampleRequest(
    @Schema(description = "Human-readable name.", maxLength = 200) @NullOrNotBlank @Size(max = 200)
        String name,
    @Schema(description = "Free-form description.") String description,
    @Schema(description = "Quantity on hand. Must be >= 0.") @Min(0) Integer quantity,
    @Schema(description = "Unit price. Must be >= 0.")
        @DecimalMin("0.0")
        @Digits(integer = 17, fraction = 2)
        BigDecimal price,
    @Schema(description = "Business event timestamp (UTC).") Instant occurredAt,
    @Schema(description = "Lifecycle status.") ExampleStatus status,
    @Schema(description = "Free-form tags. When provided, replaces the entire set.")
        Set<@NotBlank @Size(max = 64) String> tags) {}
