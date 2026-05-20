package io.github.mykhailokulakov.genericspringservice.security;

public enum Role {
  ADMIN,
  USER;

  public String authority() {
    return "ROLE_" + name();
  }
}
