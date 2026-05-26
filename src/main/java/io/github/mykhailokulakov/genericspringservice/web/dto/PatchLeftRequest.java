package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.github.mykhailokulakov.genericspringservice.common.validation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Partial-update payload for a Left.")
public record PatchLeftRequest(
    @Schema(description = "Code.", maxLength = 100) @NullOrNotBlank @Size(max = 100) String code) {}
