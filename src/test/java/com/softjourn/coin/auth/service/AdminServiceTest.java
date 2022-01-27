package com.softjourn.coin.auth.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.softjourn.coin.auth.config.ApplicationProperties;
import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.exception.DeletingSuperUserException;
import com.softjourn.coin.auth.exception.DuplicateEntryException;
import com.softjourn.coin.auth.exception.LDAPNotFoundException;
import com.softjourn.coin.auth.exception.NoSuchUserException;
import com.softjourn.coin.auth.exception.NotValidRoleException;
import com.softjourn.coin.auth.repository.UserRepository;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.naming.ConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@EnableJpaRepositories(basePackages = "com.softjourn.coin.auth.repository")
@EntityScan(basePackages = "com.softjourn.coin.auth.entity")
@SpringBootTest(classes = {RoleService.class})
public class AdminServiceTest {

    private final Role testRole = new Role("ROLE_TEST");
    private final Role testRoleUpdate = new Role("ROLE_TEST_UPDATE");
    private final User testUser = new User("ldap_test", "full_name"
            , "email@email", Collections.singleton(testRole));
    private final User testUserWithWrongName = new User("ldap_test", "wrong_name"
            , "email@email", Collections.singleton(testRole));
    private final User testUserThatNotExistsInLDAP = new User("illegal_user", "full_name"
            , "email@email", null);

    @Value("${super.roles}")
    String[] superRoles;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private UserRepository userRepository;
    @Mock
    private ILdapService ldapService;

    @Autowired
    private RoleService roleService;
    private AdminService adminService;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Value("${super.admins}")
    private String[] superUsers;
    private User testSuperUser;
    private User testSuperUserInDB;

    @Before
    public void init() throws ConfigurationException {
        //Injected Mock set up
        adminService = new AdminService(userRepository, ldapService, applicationProperties);
    }

    @Before
    public void setUpSuperUsers() throws ConfigurationException {
        this.testSuperUser = new User("new_super_user", "full_name", "email@email"
                , Collections.singleton(new Role(superRoles[0], true)));
        this.testSuperUserInDB = new User(superUsers[0], "FULL NAME", "EMAIL@email"
                , Collections.singleton(new Role(superRoles[0], true)));
    }

    @Before
    public void setUp() throws ConfigurationException {

        //Post construct init super user
        when(ldapService.getUser(superUsers[0])).thenReturn(new User(superUsers[0], "FULL NAME", "EMAIL@email", null));
        // set up roles
        this.roleService.add(testRole);

        //legal test user ldap
        when(ldapService.userExist(testSuperUserInDB)).thenReturn(true);
        when(ldapService.userExist(testUser)).thenReturn(true);
        when(ldapService.userExist(testSuperUser)).thenReturn(true);

    }

    @Test(expected = NotValidRoleException.class)
    public void add_ValidUserWithNotValidRole_NotValidRoleException() {
        Role notValidRole = new Role("ROLE_NOT_VALID");
        testUser.setAuthorities(Collections.singleton(notValidRole));
        adminService.add(testUser);
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
        adminService.add(testUser);
        adminService.add(testUser);
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

    private void updateTestFunction(User user, Role testRole) throws Exception {
        assertEquals(adminService.add(testUser), testUser);
        Set<Role> authorities = user.getAuthorities();
        Set<Role> newAuth = new HashSet<>(authorities);
        newAuth.add(testRole);
        user.setAuthorities(newAuth);
        assertNotEquals(user.getAuthorities(), authorities);
        assertEquals(adminService.update(user), user);
        assertEquals(adminService.find(user.getLdapId()), user);
    }

    @Test(expected = NotValidRoleException.class)
    //Role update does not exists
    public void update_testUserWithNotValidRoleUpdate_NotValidRoleException() throws Exception {
        this.updateTestFunction(testUser, testRoleUpdate);
    }

    @Test
    // Role update exists
    public void update_testUserWithValidRoleUpdate_testUserWithValidRoleUpdate() throws Exception {
        this.roleService.add(testRoleUpdate);
        this.updateTestFunction(testUser, testRoleUpdate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_testUserWithEmptyAuthoritiesSet_IllegalArgumentException() throws Exception {
        assertEquals(adminService.add(testUser), testUser);
        testUser.setAuthorities(null);
        assertEquals(adminService.update(testUser), testUser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_testUserWithEmptyAuthority_IllegalArgumentException() throws Exception {
        assertEquals(adminService.add(testUser), testUser);
        testUser.setAuthorities(new HashSet<>());
        adminService.update(testUser);
    }

    @Test(expected = LDAPNotFoundException.class)
    public void update_userDoesNotExist_LDAPNotFoundException() throws Exception {
        adminService.update(testUserThatNotExistsInLDAP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_TestUser_NameThatDiffersFromTestUserName_ConflictException() throws Exception {
        adminService.update(testUser, "NameThatDiffersFromTestUserName");
    }

    @Test
    public void update_TestUser_TestUserName_TestUser() throws Exception {
        assertEquals(adminService.add(testUser), testUser);
        assertEquals(adminService.update(testUser, testUser.getLdapId()), testUser);
    }

    @Test(expected = NoSuchUserException.class)
    public void update_TestUserNotAdmin_NoSuchUserException() throws Exception {
        adminService.update(testUser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_SuperUser_Exception() throws Exception {
        assertEquals(adminService.find(testSuperUserInDB.getLdapId()), testSuperUserInDB);
        testSuperUserInDB.setAuthorities(Collections.singleton(testRole));
        adminService.update(testSuperUserInDB);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_testUserWithSuperRole_Exception() throws Exception {
        assertEquals(adminService.add(testUser), testUser);
        assertEquals(adminService.find(testUser.getLdapId()), testUser);
        testUser.setAuthorities(Collections.singleton(new Role(superRoles[0], true)));
        adminService.update(testUser);
    }
}
