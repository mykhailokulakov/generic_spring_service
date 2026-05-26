package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Full replacement payload for a Child.")
public record UpdateChildRequest(
    @Schema(description = "Value.", example = "child-1", maxLength = 200) @NotBlank @Size(max = 200)
        String value) {}
