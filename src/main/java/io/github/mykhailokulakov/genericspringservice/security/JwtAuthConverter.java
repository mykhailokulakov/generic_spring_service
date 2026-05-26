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
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Map<String, Object> realm = jwt.getClaim("realm_access");
    if (realm == null) {
      return new JwtAuthenticationToken(jwt, List.of(), jwt.getSubject());
    }
    var rolesObj = realm.getOrDefault("roles", List.of());
    if (!(rolesObj instanceof Collection<?> rawRoles)) {
      return new JwtAuthenticationToken(jwt, List.of(), jwt.getSubject());
    }
    var authorities =
        rawRoles.stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
            .toList();
    return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
  }
}
