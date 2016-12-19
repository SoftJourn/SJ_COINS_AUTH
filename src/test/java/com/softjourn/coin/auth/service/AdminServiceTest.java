package com.softjourn.coin.auth.service;

import com.softjourn.coin.auth.config.AuthTestConfiguration;
import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.exception.DeletingSuperUserException;
import com.softjourn.coin.auth.exception.DuplicateEntryException;
import com.softjourn.coin.auth.exception.LDAPNotFoundException;
import com.softjourn.coin.auth.exception.NotValidRoleException;
import com.softjourn.coin.auth.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.ConfigurationException;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@DataJpaTest
@SpringBootTest(classes = AuthTestConfiguration.class)
@ContextConfiguration(classes = {RoleService.class})
public class AdminServiceTest {

    private final Role testRole = new Role("ROLE_TEST");
    private final User testUser = new User("ldap_test", "full_name"
            , "email@email", Collections.singleton(testRole));
    private final User testUserWithWrongName = new User("ldap_test", "wrong_name"
            , "email@email", Collections.singleton(testRole));
    private final User testUserAtLDAP = new User("ldap_test", "full_name"
            , "email@email", null);
    private final User testUserThatNotExistsInLDAP = new User("illegal_user", "full_name"
            , "email@email", null);
    @Value("${super.roles}")
    String[] superRoles;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private UserRepository userRepository;
    @Mock
    private LdapService ldapService;
    @Autowired
    private RoleService roleService;
    private AdminService adminService;
    @Value("${super.admins}")
    private String[] superUsers;
    private User testSuperUser;
    private User testSuperUserAtLDAP;

    @Before
    public void setUpSuperUsers() throws ConfigurationException {
        this.testSuperUser = new User("new_super_user", "full_name", "email@email"
                , Collections.singleton(new Role(superRoles[0], true)));
        this.testSuperUserAtLDAP = new User("new_super_user", "full_name", "email@email"
                , null);

    }

    @Before
    public void setUp() throws ConfigurationException {

        // set up roles
        this.roleService.add(testRole);

        when(ldapService.userExist("new_super_user")).thenReturn(true);

        //legal test user ldap
        when(ldapService.userExist(testUser.getLdapId())).thenReturn(true);
        //legal test users in ldap
        when(ldapService.userExist(testUser.getLdapId())).thenReturn(true);
        when(ldapService.userExist("ldap_test_valid")).thenReturn(true);
        when(ldapService.getUser("ldap_test_valid")).thenReturn(
                new User("ldap_test_valid", "full_name", "email@email", null));
        //return test user
        when(ldapService.getUser(testUser.getLdapId())).thenReturn(testUserAtLDAP);

        //for init super user in application start up
        when(ldapService.getUser(superUsers[0])).thenReturn(new User(superUsers[0], "FULL NAME", "EMAIL@email", null));
        when(ldapService.getUser(testSuperUser.getLdapId())).thenReturn(testSuperUserAtLDAP);

        //Injected Mock set up
        adminService = new AdminService(userRepository, ldapService, superUsers, superRoles);
    }

    @Test(expected = NotValidRoleException.class)
    public void add_ValidUserWithNotValidRole_NotValidRoleException() {
        Role notValidRole = new Role("ROLE_NOT_VALID");
        User notValidUser = new User("ldap_test_valid", "full_name", "email@email"
                , Collections.singleton(notValidRole));
        adminService.add(notValidUser);
    }

    @Test
    public void add_RoleValid_Arg() {
        assertEquals(roleService.add(testRole), testRole);
        assertEquals(adminService.add(testUser), testUser);
        adminService.delete(testUser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_UserWithSuperRole_IllegalArgumentException() throws Exception {
        adminService.add(testSuperUser);
    }

    @Test(expected = LDAPNotFoundException.class)
    public void add_TestUserThatNotExistsInLDAP_LDAPNotFoundException() throws Exception {
        adminService.add(testUserThatNotExistsInLDAP);
    }

    @Test(expected = DuplicateEntryException.class)
    public void add_DuplicateUser_ThrowsException() throws Exception {
        try {
            adminService.add(testUser);
            adminService.add(testUser);
        } finally {
            adminService.delete(testUser);
        }

    }

    @Test
    public void init_SuperUserLDAP_SuperUser() {
        assertNotNull(adminService.find(superUsers[0]));
    }

    @Test(expected = DeletingSuperUserException.class)
    public void delete_SuperUser_ThrowsException() {
        User superUser = adminService.find(superUsers[0]);
        assertNotNull(superUser);
        adminService.delete(superUser.getLdapId());
    }

    @Test
    public void delete_ExistsAdmin_ActualDelete() {
        assertEquals(roleService.add(testRole), testRole);
        assertEquals(adminService.add(testUser), testUser);
        assertEquals(adminService.find(testUser.getLdapId()), testUser);
        adminService.delete(testUser.getLdapId());
        assertNull(adminService.find(testUser.getLdapId()));
    }

    @Test
    public void isAdmin_Admin_True() throws Exception {
        assertEquals(roleService.add(testRole), testRole);
        assertEquals(adminService.add(testUser), testUser);
        assertTrue(adminService.isAdmin(testUser.getLdapId()));
        adminService.delete(testUser.getLdapId());
    }

    @Test
    public void isAdmin_NotAdmin_False() throws Exception {
        adminService.isAdmin("Not Admin");
    }

    @Test
    public void add_ValidAdmin_ValidAdmin() throws Exception {
        assertEquals(adminService.add(testUser), testUser);
    }

    @Test(expected = LDAPNotFoundException.class)
    public void add_NotValidAdminWithWrongSurname_NotFoundException() throws Exception {
        adminService.add(testUserWithWrongName);
    }

    @Test
    public void updateAdmin() throws Exception {

    }

}
