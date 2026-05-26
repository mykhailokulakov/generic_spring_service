package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.github.mykhailokulakov.genericspringservice.common.validation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Partial-update payload for a Parent.")
public record PatchParentRequest(
    @Schema(description = "Label.", maxLength = 200) @NullOrNotBlank @Size(max = 200)
        String label) {}
