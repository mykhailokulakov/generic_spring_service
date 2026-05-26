package io.github.mykhailokulakov.genericspringservice.domain.model;

import java.util.UUID;

public record ChildFilter(String value, UUID parentId) {
  public static ChildFilter empty() {
    return new ChildFilter(null, null);
  }
}
