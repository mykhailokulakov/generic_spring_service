package io.github.mykhailokulakov.genericspringservice.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoleHierarchyResolverTest {

  private final RoleHierarchyResolver resolver = new RoleHierarchyResolver();

  @Test
  void userTier_matchesEnumDefinition() {
    String[] expected = RoleTier.USER_TIER.roles().stream().map(Enum::name).toArray(String[]::new);
    assertThat(resolver.userTier()).containsExactlyInAnyOrder(expected);
  }

  @Test
  void adminTier_matchesEnumDefinition() {
    String[] expected = RoleTier.ADMIN_TIER.roles().stream().map(Enum::name).toArray(String[]::new);
    assertThat(resolver.adminTier()).containsExactlyInAnyOrder(expected);
  }

  @Test
  void userTier_admitsUserAndAdmin() {
    assertThat(resolver.userTier()).containsExactlyInAnyOrder(Role.USER.name(), Role.ADMIN.name());
  }

  @Test
  void adminTier_admitsAdminOnly() {
    assertThat(resolver.adminTier()).containsExactly(Role.ADMIN.name());
  }

  @Test
  void authority_prefixesRoleName() {
    assertThat(Role.USER.authority()).isEqualTo("ROLE_USER");
    assertThat(Role.ADMIN.authority()).isEqualTo("ROLE_ADMIN");
  }
}
