package com.softjourn.coin.auth.config;

import com.softjourn.coin.auth.service.ILdapService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

public class ExtraFieldTokenEnhancer extends JwtAccessTokenConverter{

  @Autowired
  private ILdapService ldapService;

  @Override
  public OAuth2AccessToken enhance(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {
    LdapUserDetailsImpl userDetails = (LdapUserDetailsImpl) oAuth2Authentication.getPrincipal();
    final Map<String, Object> additionalInfo = new HashMap<>();
    com.softjourn.coin.auth.entity.User ldapUser = ldapService.getUser(userDetails.getUsername());
    additionalInfo.put("email", ldapUser.getEmail());

    ((DefaultOAuth2AccessToken) oAuth2AccessToken).setAdditionalInformation(additionalInfo);
    // String encoded = super.encode(oAuth2AccessToken, oAuth2Authentication);
    // ((DefaultOAuth2AccessToken) oAuth2AccessToken).setValue(encoded);

    return super.enhance(oAuth2AccessToken, oAuth2Authentication);
  }
}
