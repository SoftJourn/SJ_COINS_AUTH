package com.softjourn.coin.auth.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.exception.NoSuchUserException;
import com.softjourn.coin.auth.service.AdminService;
import com.softjourn.coin.auth.utility.OAuthHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
// Creates embedded database
// Spring Boot can auto-configure embedded H2, HSQL and Derby databases
@AutoConfigureTestDatabase
@AutoConfigureRestDocs("target/generated-snippets")
@AutoConfigureMockMvc(secure = false)
public class AdminControllerTest {

    private final Role testRole = new Role("ROLE_TEST");
    private final User testUser = new User("ldap_test", "full_name"
            , "email@email", new HashSet<Role>() {{
        add(testRole);
    }});
    private final User testUserNotAdmin = new User("testUserNotAdmin", "full_name"
            , "email@email", new HashSet<Role>() {{
        add(testRole);
    }});

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OAuthHelper authHelper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AdminService adminService;

    @Autowired
    private ObjectMapper mapper;
    private FieldDescriptor[] role = new FieldDescriptor[]{
            fieldWithPath("authority").description("Role name with prefix 'ROLE_'"),
            fieldWithPath("superRole").description("Bool value. Is this role super role?")
    };
    private FieldDescriptor[] user = new FieldDescriptor[]{
            fieldWithPath("ldapId").description("vpupkin"),
            fieldWithPath("fullName").description("Vasuliy Pupkin"),
            fieldWithPath("email").description("vpupkin@softjoun.com"),
            fieldWithPath("authorities").description("ROLE_ADMIN")

    };

    @Before
    public void setUp() throws Exception {
        when(adminService.getAdmins()).thenReturn(new ArrayList<User>() {{
            add(testUser);
        }});
        when(adminService.add(testUser)).thenReturn(testUser);
        when(adminService.update(testUser)).thenReturn(testUser);
        when(adminService.update(testUserNotAdmin, testUserNotAdmin.getLdapId())).thenThrow(new NoSuchUserException("User is not admin афіва"));
    }

    private String json(Object o) throws IOException {
        return mapper.writeValueAsString(o);
    }

    @Test
    public void getAll_WithoutRole_UnauthorizedRequest() throws Exception {
        mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/v1/admin")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAll_WithRoleUser_Forbidden() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_USER");

        mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/v1/admin")
                        .with(bearerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getAll_WithRoleSuperUser_AllUser() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_SUPER_ADMIN");

        mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/v1/admin")
                        .with(bearerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getAll_WithRoleUserManager_AllUser() throws Exception {

        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_USER_MANAGER");

        mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/v1/admin")
                        .with(bearerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get_all_admins", preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[]").description("An array of users"))
                                .andWithPrefix("[].", user)
                                .andWithPrefix("[].authorities.[].", role)));

    }

    @Test
    public void addNewAdmin_testUserWithRoleUserManger_testUser() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_USER_MANAGER");
        mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/v1/admin")
                        .content(json(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(bearerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("add_new_admin"
                        , preprocessRequest(prettyPrint())
                        , preprocessResponse(prettyPrint())
                        , requestFields(user).andWithPrefix("authorities.[].", role)));
    }

    @Test
    public void addNewAdmin_testUserWithSuperAdmin_testUser() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_SUPER_ADMIN");
        mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/v1/admin")
                        .content(json(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(bearerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void addNewAdmin_testUserWithoutToken_Unauthorized() throws Exception {
        mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/v1/admin")
                        .content(json(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void addNewAdmin_testUserWithRoleUser_Forbidden() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_USER");
        mvc.perform(RestDocumentationRequestBuilders
                .post("/v1/admin")
                .content(json(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .with(bearerToken)
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    public void updateAdmin_testUserWithRoleSuperAdmin_testUser() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_SUPER_ADMIN");
        mvc.perform(RestDocumentationRequestBuilders
                .post("/v1/admin")
                .content(json(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .with(bearerToken)
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk());
    }

    @Test
    public void updateAdmin_testUserWithRoleUserManager_testUser() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_USER_MANAGER");
        mvc.perform(RestDocumentationRequestBuilders
                .post("/v1/admin")
                .content(json(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .with(bearerToken)
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andDo(document("update_admin"
                        , preprocessRequest(prettyPrint())
                        , preprocessResponse(prettyPrint())
                        , requestFields(user).andWithPrefix("authorities.[].", role)));
    }

    @Test
    public void updateAdmin_testUserWithRoleUser_Forbidden() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_USER");
        mvc.perform(RestDocumentationRequestBuilders
                .post("/v1/admin")
                .content(json(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .with(bearerToken)
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    public void updateAdmin_testUserNotAdmin_HttpStatusNotFound() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_SUPER_ADMIN");
        mvc.perform(RestDocumentationRequestBuilders
                .post("/v1/admin/" + testUserNotAdmin.getLdapId())
                .content(json(testUserNotAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .with(bearerToken)
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound());
    }

    @Test
    public void delete_testUserWithRoleSuperAdmin_testUser() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_SUPER_ADMIN");
        mvc.perform(RestDocumentationRequestBuilders
                .delete("/v1/admin/" + testUser.getLdapId())
                .with(bearerToken)
        )
                .andExpect(status().isOk())
                .andDo(document("delete_admin"
                        , preprocessRequest(prettyPrint())
                        , preprocessResponse(prettyPrint())));
    }

    @Test
    public void delete_testUserWithRoleUserManager_testUser() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_USER_MANAGER");
        mvc.perform(RestDocumentationRequestBuilders
                .delete("/v1/admin/" + testUser.getLdapId())
                .with(bearerToken)
        )
                .andExpect(status().isOk());
    }

    @Test
    public void delete_testUserWithRoleUser_Forbidden() throws Exception {
        RequestPostProcessor bearerToken = authHelper.withUser("test", "ROLE_USER");
        mvc.perform(RestDocumentationRequestBuilders
                .delete("/v1/admin/" + testUser.getLdapId())
                .with(bearerToken)
        )
                .andExpect(status().isForbidden());
    }


}


