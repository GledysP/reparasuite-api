package com.reparasuite.api.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtRoleAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = new ArrayList<>();

    Object rolClaim = jwt.getClaims().get("rol");
    if (rolClaim instanceof String rol && !rol.isBlank()) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.trim().toUpperCase()));
    }

    String principalName = resolvePrincipalName(jwt);
    return new JwtAuthenticationToken(jwt, authorities, principalName);
  }

  private String resolvePrincipalName(Jwt jwt) {
    Object usuario = jwt.getClaims().get("usuario");
    if (usuario instanceof String s && !s.isBlank()) {
      return s;
    }

    Object email = jwt.getClaims().get("email");
    if (email instanceof String s && !s.isBlank()) {
      return s;
    }

    return jwt.getSubject();
  }
}