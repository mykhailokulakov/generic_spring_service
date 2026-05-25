package io.github.mykhailokulakov.genericspringservice.domain.model;

import java.time.Instant;
import java.util.UUID;

public interface DomainModel {

  UUID id();

  Instant createdAt();

  Instant updatedAt();

  Long version();
}
