package io.github.mykhailokulakov.genericspringservice.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoleHierarchyResolverTest {

  private final RoleHierarchyResolver resolver = new RoleHierarchyResolver();

  @Test
  void givenUserTier_whenResolved_thenMatchesEnumDefinition() {
    var expected = RoleTier.USER_TIER.roles().stream().map(Enum::name).toArray(String[]::new);

    var resolved = resolver.userTier();

    assertThat(resolved).containsExactlyInAnyOrder(expected);
  }

  @Test
  void givenAdminTier_whenResolved_thenMatchesEnumDefinition() {
    var expected = RoleTier.ADMIN_TIER.roles().stream().map(Enum::name).toArray(String[]::new);

    var resolved = resolver.adminTier();

    assertThat(resolved).containsExactlyInAnyOrder(expected);
  }

  @Test
  void givenUserTier_whenResolved_thenAdmitsBothUserAndAdmin() {
    var resolved = resolver.userTier();

    assertThat(resolved).containsExactlyInAnyOrder(Role.USER.name(), Role.ADMIN.name());
  }

  @Test
  void givenAdminTier_whenResolved_thenAdmitsAdminOnly() {
    var resolved = resolver.adminTier();

    assertThat(resolved).containsExactly(Role.ADMIN.name());
  }

  @Test
  void givenRole_whenAuthorityRequested_thenPrefixesNameWithRolePrefix() {
    assertThat(Role.USER.authority()).isEqualTo("ROLE_USER");
    assertThat(Role.ADMIN.authority()).isEqualTo("ROLE_ADMIN");
  }
}
