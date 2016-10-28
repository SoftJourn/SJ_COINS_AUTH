package com.softjourn.coin.auth.controller;

import com.softjourn.coin.auth.config.AuthTestConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.templates.TemplateFormats;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthTestConfiguration.class)
@WebAppConfiguration
public class TokenControllerTest {

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TokenEndpoint tokenEndpoint;

    private MockMvc mockMvc;

    Authentication authentication;

    @Before
    public synchronized void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)
                        .snippets()
                        .withTemplateFormat(TemplateFormats.asciidoctor()))

                .build();

        authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn((Principal) () -> "client");
        when(authentication.getName()).thenReturn("client");
        when(authentication.isAuthenticated()).thenReturn(true);

        tokenEndpoint.setTokenGranter((grantType, tokenRequest) -> new OAuth2AccessToken() {
            @Override
            public Map<String, Object> getAdditionalInformation() {
                return Collections.emptyMap();
            }

            @Override
            public Set<String> getScope() {
                return new HashSet<String>() {{
                    add("read");
                    add("write");
                }};
            }

            @Override
            public OAuth2RefreshToken getRefreshToken() {
                return new OAuth2RefreshToken() {
                    @Override
                    public String getValue() {
                        return "RefreshTokenValue";
                    }
                };
            }

            @Override
            public String getTokenType() {
                return "bearer";
            }

            @Override
            public boolean isExpired() {
                return false;
            }

            @Override
            public Date getExpiration() {
                return new Date(1000000);
            }

            @Override
            public int getExpiresIn() {
                return 5000;
            }

            @Override
            public String getValue() {
                return "accessToken";
            }
        });
    }

    @Test
    @WithMockUser
    public void testGetAccessTokenImplicitFlow() throws Exception {
        mockMvc
                .perform(RestDocumentationRequestBuilders
                        .get("/oauth/authorize")
                        .param("response_type", "token")
                        .param("client_id", "client")
                        .param("scope", "read write")
                        .principal(authentication)
                )
                .andExpect(status().is(302))
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("client.redirect.uri#access_token=")))
                .andDo(document("access_token_implicit_flow", preprocessResponse(prettyPrint())));
    }


    @Test
    public void testGetAccessTokenAuthorizationFlow() throws Exception {
        mockMvc
                .perform(RestDocumentationRequestBuilders
                        .get("/oauth/authorize")
                        .param("response_type", "code")
                        .param("redirect_uri", "client.redirect.uri")
                        .param("client_id", "client")
                        .param("scope", "read write")
                        .principal(authentication)
                )
                .andExpect(status().is(302))
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("client.redirect.uri?code=")))
                .andDo(document("access_token_auth_flow", preprocessResponse(prettyPrint())));
    }

    @Test
    public void testGetAccessTokenAuthorizationFlowCodePart() throws Exception {
        mockMvc
                .perform(RestDocumentationRequestBuilders
                        .post("/oauth/token")
                        .header(HttpHeaders.AUTHORIZATION, "Basic Y2xpZW50PXNlY3JldA==")
                        .param("code", "codeValue")
                        .param("grant_type", "authorization_code")
                        .param("redirect_uri", "client.redirect.uri")
                        .principal(authentication)
                )
                .andExpect(status().isOk())
                .andDo(document("access_token_authorization_code", preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("access_token").description("The access token issued by the authorization server."),
                                fieldWithPath("refresh_token").description("he refresh token which can be used to obtain new access tokens."),
                                fieldWithPath("token_type").description("Access token type"),
                                fieldWithPath("expires_in").description("The lifetime in seconds of the access token."),
                                fieldWithPath("scope").description("Scope you can access with this token.")
                        )));
    }


    @Test
    public void testGetAccessTokenPasswordFlow() throws Exception {
        mockMvc
                .perform(RestDocumentationRequestBuilders
                        .post("/oauth/token")
                        .principal(authentication)
                        .header(HttpHeaders.AUTHORIZATION, "Basic Y2xpZW50PXNlY3JldA==")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "yourLDAPid")
                        .param("password", "yourLDAPpassword")
                        .param("grant_type", "password")
                )//[client_id=client_secret(encoded in base64)]
                .andExpect(status().isOk())
                .andDo(document("access_token", preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("access_token").description("The access token issued by the authorization server."),
                                fieldWithPath("refresh_token").description("he refresh token which can be used to obtain new access tokens."),
                                fieldWithPath("token_type").description("Access token type"),
                                fieldWithPath("expires_in").description("The lifetime in seconds of the access token."),
                                fieldWithPath("scope").description("Scope you can access with this token.")
                        )));
    }

    @Test
    public void testExchangeAccessTokenForRefreshToken() throws Exception {
        mockMvc
                .perform(RestDocumentationRequestBuilders
                        .post("/oauth/token")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("refresh_token", "{refreshTokenValue}")
                        .param("grant_type", "refresh_token")
                )
                .andExpect(status().isOk())
                .andDo(document("refresh_access_token", preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("access_token").description("The access token issued by the authorization server."),
                                fieldWithPath("refresh_token").description("he refresh token which can be used to obtain new access tokens."),
                                fieldWithPath("token_type").description("Access token type"),
                                fieldWithPath("expires_in").description("The lifetime in seconds of the access token."),
                                fieldWithPath("scope").description("Scope you can access with this token.")
                        )));
    }

    @Test
    public void testRevokeRefreshToken() throws Exception {
        mockMvc
                .perform(RestDocumentationRequestBuilders
                        .post("/oauth/token/revoke")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("token_value", "{refreshTokenValue}")
                )
                .andExpect(status().isOk())
                .andDo(document("revoke_refresh_token", preprocessResponse(prettyPrint())));
    }

}