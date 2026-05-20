package io.github.mykhailokulakov.genericspringservice.security;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = RequiresUserSmokeTest.SmokeController.class)
@Import({
  RequiresUserSmokeTest.SmokeController.class,
  RequiresUserSmokeTest.SmokeSecurityConfig.class,
  RoleHierarchyResolver.class
})
class RequiresUserSmokeTest {

  @Autowired private WebApplicationContext context;
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(roles = "USER")
  void userTierUser_returns200() throws Exception {
    mvc.perform(get("/_smoke/requires-user")).andExpect(status().isOk());
  }

  @Test
  void unauthenticated_returns403() throws Exception {
    mvc.perform(get("/_smoke/requires-user")).andExpect(status().isForbidden());
  }

  @RestController
  static class SmokeController {
    @GetMapping("/_smoke/requires-user")
    @RequiresUser
    String ping() {
      return "ok";
    }
  }

  @EnableWebSecurity
  @EnableMethodSecurity
  static class SmokeSecurityConfig {
    @Bean
    SecurityFilterChain smokeFilterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .exceptionHandling(ex -> ex.authenticationEntryPoint(new Http403ForbiddenEntryPoint()));
      return http.build();
    }
  }
}
