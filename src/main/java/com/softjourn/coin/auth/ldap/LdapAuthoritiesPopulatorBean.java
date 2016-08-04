package com.softjourn.coin.auth.ldap;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthority;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Slf4j
@Component
public class LdapAuthoritiesPopulatorBean implements LdapAuthoritiesPopulator {

    @Value("${superAdminLdapName}")
    String superAdminLdapName;

    @Autowired
    LdapService ldapService;

    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
        List<GrantedAuthority> result = new ArrayList<>();

        LdapAuthoritiesPopulatorBean.SJLDAPAuthority authority = isAdmin(username) ?
                new SJLDAPAuthority("ROLE_ADMIN", "dc=admin,ou=People,ou=Users,dc=ldap,dc=sjua") :
                new SJLDAPAuthority("ROLE_USER", "dc=user,ou=People,ou=Users,dc=ldap,dc=sjua");

        authority.setFullName(getAttribute(userData, "cn"));
        authority.setEmail(getAttribute(userData, "mail"));
        result.add(authority);

        return result;
    }

    private boolean isAdmin(String userName) {
        return userName.equals(superAdminLdapName) || ldapService.isAdmin(userName);
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
