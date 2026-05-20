package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Schema(
    description =
        "Full replacement payload for an Example. PUT semantics: every field is required.")
public record UpdateExampleRequest(
    @Schema(description = "Human-readable name.", example = "Widget A", maxLength = 200)
        @NotBlank
        @Size(max = 200)
        String name,
    @Schema(description = "Free-form description.") @NotNull String description,
    @Schema(description = "Quantity on hand. Must be >= 0.", example = "10") @NotNull @Min(0)
        Integer quantity,
    @Schema(description = "Unit price. Must be >= 0.", example = "19.99")
        @NotNull
        @DecimalMin("0.0")
        @Digits(integer = 17, fraction = 2)
        BigDecimal price,
    @Schema(description = "Business event timestamp (UTC).") @NotNull Instant occurredAt,
    @Schema(description = "Lifecycle status.", example = "ACTIVE") @NotNull ExampleStatus status,
    @Schema(description = "Free-form tags.") @NotNull Set<@NotBlank @Size(max = 64) String> tags) {}
