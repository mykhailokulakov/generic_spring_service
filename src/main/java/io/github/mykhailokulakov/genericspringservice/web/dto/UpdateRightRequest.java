package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    description = "Full replacement payload for a Right. PUT semantics: every field is required.")
public record UpdateRightRequest(
    @Schema(description = "Human-readable name.", example = "READ_USERS", maxLength = 200)
        @NotBlank
        @Size(max = 200)
        String name) {}
