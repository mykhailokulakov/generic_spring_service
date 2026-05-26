package io.github.mykhailokulakov.genericspringservice.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mykhailokulakov.genericspringservice.common.validation.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record Owner(
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) UUID id,
    @NotBlank @Size(max = 200) String handle,
    @NotNull(groups = OnCreate.class) UUID exampleId,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) Instant createdAt,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) Instant updatedAt,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long version)
    implements DomainModel {}
