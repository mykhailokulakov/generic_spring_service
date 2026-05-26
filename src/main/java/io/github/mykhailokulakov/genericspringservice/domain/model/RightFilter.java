package io.github.mykhailokulakov.genericspringservice.domain.model;

public record RightFilter(String name) {
  public static RightFilter empty() {
    return new RightFilter(null);
  }
}
