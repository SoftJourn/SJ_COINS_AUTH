package com.softjourn.coin.auth.controller;

import com.softjourn.coin.auth.config.ApplicationProperties;
import com.softjourn.coin.auth.exception.LDAPNotFoundException;
import com.softjourn.coin.auth.service.ILdapService;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequiredArgsConstructor
public class LoginController {

  private final ILdapService ldapService;
  private final AuthorizationServerTokenServices tokenStore;
  private final ClientDetailsService clientDetailsService;
  private final ApplicationProperties applicationProperties;

  @GetMapping("/login")
  public String login(Model model, @RequestParam(required = false) String error) {
    if (error != null) {
      model.addAttribute("error", 1);
    }
    return "login";
  }

  @PostMapping("/login/passwordless/{ldapId:.+}")
  public ResponseEntity<OAuth2AccessToken> passwordlessLogin(
      @RequestHeader("X-CLIENT-SECRET") String clientSecret,
      @PathVariable("ldapId") String ldapId
  ) {
    ClientDetails details = clientDetailsService
        .loadClientByClientId(applicationProperties.getAuth().getBiometric().getClientId());

    if (!clientSecret.equals(details.getClientSecret())) {
      throw new AccessDeniedException(null);
    }

    final com.softjourn.coin.auth.entity.User user = ldapService.getUser(ldapId);

    if (Objects.isNull(user)) {
      throw new LDAPNotFoundException(ldapId);
    }

    final AuthorizationRequest authorizationRequest = new AuthorizationRequest();

    authorizationRequest.setClientId(applicationProperties.getAuth().getBiometric().getClientId());
    authorizationRequest.setApproved(true);

    final User userPrincipal =
        new User(user.getLdapId(), "", true, true, true, true, Collections.emptySet());

    final UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            userPrincipal, null, authorizationRequest.getAuthorities());

    final OAuth2Authentication authentication =
        new OAuth2Authentication(authorizationRequest.createOAuth2Request(), authenticationToken);

    authentication.setAuthenticated(true);

    final OAuth2AccessToken accessToken = tokenStore.createAccessToken(authentication);

    return new ResponseEntity<>(accessToken, HttpStatus.OK);
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "LDAP id not found")
  @ExceptionHandler(LDAPNotFoundException.class)
  public void wrongLdapId() {
  }

  @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Incorrect client secret")
  @ExceptionHandler(AccessDeniedException.class)
  public void forbidden() {
  }
}
