package com.softjourn.coin.auth.ldap;


import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.service.AdminService;
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
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.*;


@Slf4j
@Component
public class LdapAuthoritiesPopulationBean implements LdapAuthoritiesPopulator {

    @Value("${super.admins}")
    String[] superAdminLdapName;

    private final AdminService adminService;

    @Autowired
    public LdapAuthoritiesPopulationBean(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
        final List<GrantedAuthority> result = new ArrayList<>();
        if(isAdmin(username)) {
            //roles should not be empty
            String[] roles = getUserRoles(username);
            if(roles == null)
                throw new IllegalStateException("Admin can't "+username+" have empty role set");
            Arrays.stream(roles).forEach(role ->{
                LdapAuthoritiesPopulationBean.SJLDAPAuthority authority
                        =new SJLDAPAuthority(role, "dc=admin,ou=People,ou=Users,dc=ldap,dc=sjua");
                if(userData != null) {
                    authority.setFullName(getAttribute(userData, "cn"));
                    authority.setEmail(getAttribute(userData, "mail"));
                }
                result.add(authority);
            });
        } else {
            LdapAuthoritiesPopulationBean.SJLDAPAuthority authority
                    =new SJLDAPAuthority("ROLE_USER", "dc=admin,ou=People,ou=Users,dc=ldap,dc=sjua");
            if(userData != null) {
                authority.setFullName(getAttribute(userData, "cn"));
                authority.setEmail(getAttribute(userData, "mail"));
            }
            result.add(authority);

        }
        return result;
    }

    private boolean isAdmin(String userName) {
        return adminService.isAdmin(userName);
    }


    private String[] getUserRoles(String userName){
        User user=adminService.find(userName);
        if(user==null)
            return null;
        Set<Role> roleSet=user.getAuthorities();
        return roleSet.stream().map(Role::getAuthority).toArray(String[]::new);
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
