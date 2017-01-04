package com.softjourn.coin.auth.ldap;


import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.service.AdminService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Component
public class LdapAuthoritiesPopulationBean implements LdapAuthoritiesPopulator {

    private final AdminService adminService;

    @Autowired
    public LdapAuthoritiesPopulationBean(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
        return Stream.concat(getUserRoles(username), Stream.of("ROLE_USER"))
                .map(roleName -> createAuthority(roleName, userData))
                .collect(Collectors.toList());
    }

    private SJLDAPAuthority createAuthority(String roleName, DirContextOperations userData) {
        SJLDAPAuthority authority = new SJLDAPAuthority(roleName, "ou=People,ou=Users,dc=ldap,dc=sjua");
        if(userData != null) {
            authority.setFullName(getAttribute(userData, "cn"));
            authority.setEmail(getAttribute(userData, "mail"));
        }
        return authority;
    }

    private Stream<String> getUserRoles(String userName){
        return Optional.ofNullable(adminService.find(userName))
                .flatMap(u -> Optional.ofNullable(u.getAuthorities()))
                .map(Collection::stream)
                .orElse(Collections.<Role>emptySet().stream())
                .map(Role::getAuthority);
    }

    private String getAttribute(DirContextOperations userData, String attrName) {
        try {
            Attributes attributes = userData != null ? userData.getAttributes("") : null;
            Attribute attribute = attributes != null ? attributes.get(attrName) : null;
            return (String) (attribute != null ? attribute.get(0) : null);
        } catch (NamingException e) {
            log.error("Can't retrieve attribute " + attrName + " for user " + userData);
            return null;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class SJLDAPAuthority extends LdapAuthority {

        private String fullName;

        private String email;

        SJLDAPAuthority(String role, String dn) {
            super(role, dn);
        }
    }
}
