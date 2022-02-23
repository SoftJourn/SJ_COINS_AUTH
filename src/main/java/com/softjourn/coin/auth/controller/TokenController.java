package com.softjourn.coin.auth.controller;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenController {

  private final TokenStore tokenStore;

  @PostMapping("/oauth/token/revoke")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeRefreshToken(@RequestParam("token_value") String tokenValue) {
    OAuth2RefreshToken token = tokenStore.readRefreshToken(tokenValue);
    if (Objects.isNull(token)) {
      throw new IllegalArgumentException();
    }
    tokenStore.removeRefreshToken(token);
  }

  @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Wrong token value.")  // 400
  @ExceptionHandler(IllegalArgumentException.class)
  public void wrongToken() {
  }
}
