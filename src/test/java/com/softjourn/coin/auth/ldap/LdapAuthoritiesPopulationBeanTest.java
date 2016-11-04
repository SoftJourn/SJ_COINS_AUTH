package com.softjourn.coin.auth.ldap;

import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.service.AdminService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LdapAuthoritiesPopulationBeanTest {

    @Mock
    AdminService adminService;

    @InjectMocks
    LdapAuthoritiesPopulationBean ldapAuthoritiesPopulationBean;

    @Before
    public void setUp() throws Exception {

        Role adminRole = new Role("ROLE_ADMIN", false);
        Role inspectorRole = new Role("ROLE_INSPECTOR", false);
        Role superadminRole = new Role("ROLE_SUPERADMIN", true);

        User adminUser = new User("admin", "Admin Adminow", "admin@sj.com", new HashSet<>(Arrays.asList(adminRole, inspectorRole, superadminRole)));
        User plainUser = new User("user", "User Useryak", "user@sj.com", new HashSet<>());

        when(adminService.find("admin")).thenReturn(adminUser);
        when(adminService.find("user")).thenReturn(plainUser);
    }

    @Test
    public void getGrantedAuthorities() throws Exception {
        assertEquals(1, ldapAuthoritiesPopulationBean.getGrantedAuthorities(null, "user").size());

        assertTrue(ldapAuthoritiesPopulationBean.getGrantedAuthorities(null, "user").stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(s -> s.equals("ROLE_USER"))
        );


        assertEquals(4, ldapAuthoritiesPopulationBean.getGrantedAuthorities(null, "admin").size());

        assertTrue(ldapAuthoritiesPopulationBean.getGrantedAuthorities(null, "admin").stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(s -> s.equals("ROLE_ADMIN"))
        );

        assertTrue(ldapAuthoritiesPopulationBean.getGrantedAuthorities(null, "admin").stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(s -> s.equals("ROLE_INSPECTOR"))
        );

        assertTrue(ldapAuthoritiesPopulationBean.getGrantedAuthorities(null, "admin").stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(s -> s.equals("ROLE_SUPERADMIN"))
        );

        assertTrue(ldapAuthoritiesPopulationBean.getGrantedAuthorities(null, "admin").stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(s -> s.equals("ROLE_USER"))
        );
    }



}