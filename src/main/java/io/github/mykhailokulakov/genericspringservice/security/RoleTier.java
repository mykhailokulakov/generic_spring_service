package io.github.mykhailokulakov.genericspringservice.security;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum RoleTier {
  USER_TIER(EnumSet.of(Role.USER, Role.ADMIN)),
  ADMIN_TIER(EnumSet.of(Role.ADMIN));

  private final Set<Role> roles;
  private final String[] names;

  RoleTier(Set<Role> roles) {
    this.roles = Collections.unmodifiableSet(roles);
    this.names = roles.stream().map(Enum::name).toArray(String[]::new);
  }

  public Set<Role> roles() {
    return roles;
  }

  public String[] names() {
    return names.clone();
  }
}
