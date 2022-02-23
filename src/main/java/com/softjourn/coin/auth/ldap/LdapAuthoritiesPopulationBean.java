package com.softjourn.coin.auth.ldap;

import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.service.AdminService;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LdapAuthoritiesPopulationBean implements LdapAuthoritiesPopulator {

  private static final String ROLE_USER = "ROLE_USER";

  private final AdminService adminService;

  @Override
  @Transactional(readOnly = true)
  public Collection<? extends GrantedAuthority> getGrantedAuthorities(
      DirContextOperations userData, String username
  ) {
    return Stream.concat(getUserRoles(username), Stream.of(ROLE_USER))
        .map(roleName -> createAuthority(roleName, userData))
        .collect(Collectors.toList());
  }

  private SJLDAPAuthority createAuthority(String roleName, DirContextOperations userData) {
    SJLDAPAuthority authority =
        new SJLDAPAuthority(roleName, "ou=People,ou=Users,dc=ldap,dc=sjua");
    if (Objects.nonNull(userData)) {
      authority.setFullName(getAttribute(userData, "cn"));
      authority.setEmail(getAttribute(userData, "mail"));
    }
    return authority;
  }

  private Stream<String> getUserRoles(String userName) {
    return Optional.ofNullable(adminService.find(userName))
        .flatMap(u -> Optional.ofNullable(u.getAuthorities()))
        .map(Collection::stream)
        .orElse(Stream.empty())
        .map(Role::getAuthority);
  }

  private String getAttribute(DirContextOperations userData, String attrName) {
    try {
      Attributes attributes = Objects.nonNull(userData) ? userData.getAttributes("") : null;
      Attribute attribute = Objects.nonNull(attributes) ? attributes.get(attrName) : null;
      return (String) (Objects.nonNull(attribute) ? attribute.get(0) : null);
    } catch (NamingException e) {
      log.error("Can't retrieve attribute " + attrName + " for user " + userData);
      return null;
    }
  }

  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = true)
  @ToString(callSuper = true)
  private static class SJLDAPAuthority extends LdapAuthority {

    private String fullName;
    private String email;

    SJLDAPAuthority(String role, String dn) {
      super(role, dn);
    }
  }
}
