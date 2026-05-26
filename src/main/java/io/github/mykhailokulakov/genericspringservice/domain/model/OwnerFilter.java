package io.github.mykhailokulakov.genericspringservice.domain.model;

import java.util.UUID;

public record OwnerFilter(String handle, UUID exampleId) {

  public static OwnerFilter empty() {
    return new OwnerFilter(null, null);
  }
}
