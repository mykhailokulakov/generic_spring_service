package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Full replacement payload for a Left.")
public record UpdateLeftRequest(
    @Schema(description = "Code.", example = "LEFT-001", maxLength = 100) @NotBlank @Size(max = 100)
        String code) {}
