package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating a new Left.")
public record CreateLeftRequest(
    @Schema(description = "Code.", example = "LEFT-001", maxLength = 100) @NotBlank @Size(max = 100)
        String code) {}
