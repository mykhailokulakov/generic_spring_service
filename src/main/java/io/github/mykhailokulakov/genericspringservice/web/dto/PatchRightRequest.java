package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.github.mykhailokulakov.genericspringservice.common.validation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(
    description =
        "Partial-update payload for a Right. Patch semantics: a null field means \"leave"
            + " unchanged\". There is no way to clear a field via PATCH (see DESIGN.md section"
            + " 3.4).")
public record PatchRightRequest(
    @Schema(description = "Human-readable name.", maxLength = 200) @NullOrNotBlank @Size(max = 200)
        String name) {}
