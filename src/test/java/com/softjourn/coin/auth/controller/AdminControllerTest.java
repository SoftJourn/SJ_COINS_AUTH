package com.softjourn.coin.auth.controller;


import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.service.AdminService;
import com.softjourn.coin.auth.utility.OAuthHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
// Creates embedded database
// Spring Boot can auto-configure embedded H2, HSQL and Derby databases
@AutoConfigureTestDatabase
public class AdminControllerTest {


    private final Role testRole = new Role("ROLE_TEST");
    private final User testUser = new User("ldap_test", "full_name"
            , "email@email", Collections.singleton(testRole));
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private OAuthHelper authHelper;
    private MockMvc mvc;
    @MockBean
    private AdminService adminService;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Before
    public void setUp() throws Exception {
        when(adminService.getAdmins()).thenReturn(new ArrayList<User>() {{
            add(testUser);
        }});
    }

    @Test
    public void getAll_AllUser() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_USER_MANAGER");
        ResultActions resultActions = mvc.perform(get("/api/v1/admin").with(bearerToken)).andDo(print());

        resultActions
                .andExpect(status().isOk());
    }
}


