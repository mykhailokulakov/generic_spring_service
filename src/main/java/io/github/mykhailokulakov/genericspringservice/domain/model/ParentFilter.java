package io.github.mykhailokulakov.genericspringservice.domain.model;

public record ParentFilter(String label) {
  public static ParentFilter empty() {
    return new ParentFilter(null);
  }
}
