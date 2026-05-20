package io.github.mykhailokulakov.genericspringservice.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  @SuppressWarnings("unchecked")
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Map<String, Object> realm = jwt.getClaim("realm_access");
    Collection<String> roles =
        realm == null ? List.of() : (Collection<String>) realm.getOrDefault("roles", List.of());
    var authorities = roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
    return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
  }
}
