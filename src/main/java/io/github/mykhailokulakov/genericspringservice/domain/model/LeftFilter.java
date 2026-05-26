package io.github.mykhailokulakov.genericspringservice.domain.model;

public record LeftFilter(String code) {
  public static LeftFilter empty() {
    return new LeftFilter(null);
  }
}
