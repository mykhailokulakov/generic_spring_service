package io.github.mykhailokulakov.genericspringservice.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record Left(
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) UUID id,
    @NotBlank @Size(max = 100) String code,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) Instant createdAt,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) Instant updatedAt,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long version)
    implements DomainModel {}
