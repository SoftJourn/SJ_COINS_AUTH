package com.softjourn.coin.auth.controller;

import com.softjourn.coin.auth.exception.LDAPNotFoundException;
import com.softjourn.coin.auth.service.ILdapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Value("${biometric.auth.client_id}")
    private String BIOMETRIC_AUTH_CLIENT_ID;

    private final ILdapService ldapService;
    private final AuthorizationServerTokenServices tokenStore;
    private final ClientDetailsService clientDetailsService;

    @Autowired
    public LoginController(ILdapService ldapService,
                           AuthorizationServerTokenServices tokenStore,
                           ClientDetailsService clientDetailsService) {
        this.ldapService = ldapService;
        this.tokenStore = tokenStore;
        this.clientDetailsService = clientDetailsService;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model, @RequestParam(required = false) String error) {
        if (error != null)
            model.addAttribute("error", 1);
        return "login";
    }

    @PostMapping("/login/passwordless/{ldapId:.+}")
    public ResponseEntity<OAuth2AccessToken> passwordlessLogin(@RequestHeader("X-CLIENT-SECRET") String clientSecret,
                                                               @PathVariable("ldapId") String ldapId) {
        ClientDetails details = clientDetailsService.loadClientByClientId(BIOMETRIC_AUTH_CLIENT_ID);

        if (!clientSecret.equals(details.getClientSecret())) {
            throw new AccessDeniedException(null);
        }

        final com.softjourn.coin.auth.entity.User user = ldapService.getUser(ldapId);

        if (user == null) {
            throw new LDAPNotFoundException(ldapId);
        }

        final AuthorizationRequest authorizationRequest = new AuthorizationRequest();

        authorizationRequest.setClientId(BIOMETRIC_AUTH_CLIENT_ID);
        authorizationRequest.setApproved(true);

        final User userPrincipal =
                new User(user.getLdapId(), "", true, true, true, true, Collections.emptySet());

        final UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, authorizationRequest.getAuthorities());

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
