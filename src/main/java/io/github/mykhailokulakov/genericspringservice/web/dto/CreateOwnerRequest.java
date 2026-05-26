package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Payload for creating a new Owner.")
public record CreateOwnerRequest(
    @Schema(description = "Handle.", example = "owner-a", maxLength = 200)
        @NotBlank
        @Size(max = 200)
        String handle,
    @Schema(description = "Associated example identifier.") @NotNull UUID exampleId) {}
