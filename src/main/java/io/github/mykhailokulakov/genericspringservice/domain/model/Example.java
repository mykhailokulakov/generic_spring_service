package io.github.mykhailokulakov.genericspringservice.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mykhailokulakov.genericspringservice.common.validation.OnCreate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record Example(
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) UUID id,
    @NotBlank @Size(max = 200) String name,
    String description,
    @Min(0) Integer quantity,
    @DecimalMin("0.0") @Digits(integer = 17, fraction = 2) BigDecimal price,
    Instant occurredAt,
    @NotNull(groups = OnCreate.class) ExampleStatus status,
    Set<@NotBlank @Size(max = 64) String> tags,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) Instant createdAt,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) Instant updatedAt,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long version)
    implements DomainModel {}
