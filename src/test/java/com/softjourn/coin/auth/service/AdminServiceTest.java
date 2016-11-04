package com.softjourn.coin.auth.service;

import com.softjourn.coin.auth.config.JPATestConfig;
import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.exception.DeletingSuperUserException;
import com.softjourn.coin.auth.exception.IllegalAddException;
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
@SpringBootTest(classes = JPATestConfig.class)
@DataJpaTest
@ContextConfiguration(classes = {RoleService.class})
public class AdminServiceTest {

    private AdminService adminService;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    UserRepository userRepository;

    @Mock
    LdapService ldapService;

    @Autowired
    RoleService roleService;

    @Value("${super.admins}")
    private String[] superUsers;

    @Value("${super.roles}")
    String[] superRoles;


    private final Role testRole = new Role("ROLE_TEST");
    private final User testUser = new User("ldap_test", "full_name", "email@email", Collections.singleton(testRole));

    private User testSuperUser;
    private User illegalUser;


    @Before
    public void setUp() throws ConfigurationException {
        // set up for illegal super user
        this.testSuperUser = new User("new_super_user", "full_name", "email@email", Collections.singleton(new Role(superRoles[0], true)));
        when(ldapService.userExist("new_super_user")).thenReturn(true);

        //legal test user ldap
        when(ldapService.userExist(testUser.getLdapId())).thenReturn(true);

        //for init super user in application start up
        when(ldapService.getUser(superUsers[0])).thenReturn(new User(superUsers[0], "FULL NAME", "EMAIL@email", null));

        //illegal admin user
        this.illegalUser = new User("illegal_user", "full_name", "email@email", null);

        //Injected Mock set up
        adminService = new AdminService(userRepository, ldapService, roleService, superUsers, superRoles);
    }

    @Test(expected = NotValidRoleException.class)
    public void add_NotValidRole_ThrowsException() {
        adminService.add(testUser);
    }

    @Test
    public void add_RoleValid_Arg() {
        assertEquals(roleService.add(testRole), testRole);
        assertEquals(adminService.add(testUser), testUser);
    }

    @Test(expected = IllegalAddException.class)
    public void add_UserWithSuperRole_ThrowsException() throws Exception {
        adminService.add(testSuperUser);
    }

    @Test(expected = IllegalAddException.class)
    public void add_UserThatIsNotExistsInLDAP_ThrowsException() throws Exception {
        adminService.add(illegalUser);
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

}
