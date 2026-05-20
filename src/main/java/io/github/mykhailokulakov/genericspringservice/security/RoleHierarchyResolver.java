package io.github.mykhailokulakov.genericspringservice.security;

import org.springframework.stereotype.Component;

@Component("roles")
public class RoleHierarchyResolver {

  public String[] userTier() {
    return RoleTier.USER_TIER.names();
  }

  public String[] adminTier() {
    return RoleTier.ADMIN_TIER.names();
  }
}
