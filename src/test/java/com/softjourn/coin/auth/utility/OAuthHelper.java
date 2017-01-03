package com.softjourn.coin.auth.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@TestConfiguration
public class OAuthHelper {

    @Autowired
    AuthorizationServerTokenServices tokenservice;

    public RequestPostProcessor withUser(final String username, String... authorities) {
        return mockRequest -> {
            //"user_cred"
            // Create OAuth2 token
            OAuth2Request oauth2Request = new OAuth2Request(null, "client", null, true, null, null, null, null, null);
            Authentication userauth = new TestingAuthenticationToken(username, null, authorities);
            OAuth2Authentication oauth2auth = new OAuth2Authentication(oauth2Request, userauth);
            OAuth2AccessToken token = tokenservice.createAccessToken(oauth2auth);
            // Set Authorization header to use Bearer
            mockRequest.addHeader("Authorization", "Bearer " + token.getValue());
            return mockRequest;
        };
    }

    public OAuth2AccessToken generateToken(final String username, String... authorities) {
        //"user_cred"
        // Create OAuth2 token
        OAuth2Request oauth2Request = new OAuth2Request(null, "client", null
                , true, null, null, null, null, null);
        Authentication userauth = new TestingAuthenticationToken(username, null, authorities);
        OAuth2Authentication oauth2auth = new OAuth2Authentication(oauth2Request, userauth);
        return tokenservice.createAccessToken(oauth2auth);
    }

    public RequestPostProcessor withToken(OAuth2AccessToken token) {
        return mockRequest -> {
            mockRequest.addHeader("Authorization", "Bearer " + token.getValue());
            return mockRequest;
        };
    }


}
