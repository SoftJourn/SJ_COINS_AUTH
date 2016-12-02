package com.softjourn.coin.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;

@RestController
public class TokenController{

    TokenStore tokenStore;

    @Autowired
    public TokenController(TokenStore tokenStore) {
        this.tokenStore = tokenStore;

    }

    @RequestMapping(value = "/oauth/token/revoke", method = RequestMethod.POST)
    @ResponseStatus( HttpStatus.NO_CONTENT )
    public void revokeRefreshToken(@RequestParam(name = "token_value") String tokenValue) {
        OAuth2RefreshToken token = tokenStore.readRefreshToken(tokenValue);
        if (token == null) {
            throw new IllegalArgumentException();
        }
        tokenStore.removeRefreshToken(token);
    }

    @ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Wrong token value.")  // 400
    @ExceptionHandler(IllegalArgumentException.class)
    public void wrongToken() {
        // Nothing to do
    }

}
