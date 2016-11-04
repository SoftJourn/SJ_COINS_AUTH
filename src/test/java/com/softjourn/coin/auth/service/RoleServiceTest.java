package com.softjourn.coin.auth.service;

import com.softjourn.coin.auth.config.JPATestConfig;
import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.exception.IllegalAddException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = JPATestConfig.class)
@DataJpaTest
@ContextConfiguration(classes = {RoleService.class})
public class RoleServiceTest {


    @Autowired
    private RoleService roleService;

    private final String ROLE = "ROLE_TEST";
    private final String[] SUPER_ROLES = new String[]{"ROLE_SUPER_ADMIN"};

    @Before
    public void setUp() {
    }

    @Test
    public void testAddNewRole() {

        assertNull(roleService.get(ROLE));
        assertNotNull(roleService.add(ROLE));
        //Check duplicates
        assertTrue(roleService.add(ROLE).equals(new Role(ROLE)));

    }

    @Test(expected = IllegalAddException.class)
    public void testAddSuperRole() {
        //Add Super Admin
        roleService.add(new Role(ROLE, true));
    }

    @Test
    public void testRemoveRole() {
        //Check status before assertion
        assertNull(roleService.get(ROLE));
        //Role should be added
        assertNotNull(roleService.add(ROLE));
        //Check status after assertion
        assertNotNull(roleService.get(ROLE));
        //Removing role
//

        roleService.removeRole(ROLE);
        //Role should exist
        assertNull(roleService.get(ROLE));

    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testRemoveSuperRole() {

        //Trying to delete not existed role
        try {
            roleService.removeRole(new Role("NOT_SUPER_ROLE", true));
            fail("role has been removed, but it shouldn't");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().matches(".*can't be deleted due to it is Super"));
        }

        //Trying to delete super role
        try {
            roleService.removeRole(new Role(SUPER_ROLES[0], true));
            fail("role has been removed, but it shouldn't");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().matches(".*can't be deleted due to it is Super"));
        }

        //Trying to delete super role
        try {
            roleService.removeRole(new Role(SUPER_ROLES[0]));
            fail("role has been removed, but it shouldn't");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().matches(".*can't be deleted due to it is Super"));
        }

    }

    @Test
    public void testRemoveNotExistingRole(){
        try {
            roleService.removeRole(new Role("NOT_ROLE", false));
            fail("role has been removed, but it shouldn't");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().matches(".*hasn't been found"));
        }
    }

}
