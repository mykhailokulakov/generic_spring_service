package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Payload for creating a new Child.")
public record CreateChildRequest(
    @Schema(description = "Value.", example = "child-1", maxLength = 200) @NotBlank @Size(max = 200)
        String value,
    @Schema(description = "Parent resource identifier.") @NotNull UUID parentId) {}
