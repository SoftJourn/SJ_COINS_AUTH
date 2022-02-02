package com.softjourn.coin.auth.config;

import java.net.MalformedURLException;
import java.security.KeyPair;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
@RequiredArgsConstructor
public class JwtAccessTokenConverterConfiguration {

  private final ApplicationProperties applicationProperties;

  @Bean
  public JwtAccessTokenConverter jwtAccessTokenConverter() throws MalformedURLException {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    KeyPair keyPair = new KeyStoreKeyFactory(
        new UrlResource("file:" + applicationProperties.getAuth().getKeyFileName()),
        applicationProperties.getAuth().getKeyStorePass().toCharArray())
        .getKeyPair(
            applicationProperties.getAuth().getKeyAlias(),
            applicationProperties.getAuth().getKeyMasterPass().toCharArray());
    converter.setKeyPair(keyPair);
    return converter;
  }
}
