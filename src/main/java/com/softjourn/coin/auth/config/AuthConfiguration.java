package com.softjourn.coin.auth.config;

import java.net.MalformedURLException;
import java.security.KeyPair;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.UrlResource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
public class AuthConfiguration {

  @Configuration
  @EnableAuthorizationServer
  @RequiredArgsConstructor
  public static class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationProvider authenticationProvider;
    private final TokenStore tokenStore;
    private final DataSource dataSource;
    private final UserDetailsService userDetailsService;
    private final JwtAccessTokenConverter jwtAccessTokenConverter;
    private final ClientDetailsService clientDetailsService;
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

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
      endpoints
          .authenticationManager(authenticationProvider::authenticate)
          .tokenStore(tokenStore)
          .tokenServices(defaultTokenServices(jwtAccessTokenConverter))
          .accessTokenConverter(jwtAccessTokenConverter)
          .userDetailsService(userDetailsService);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
      clients
          .jdbc(dataSource)
          .passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    @Primary
    @Qualifier("consumerTokenServices")
    public DefaultTokenServices defaultTokenServices(
        JwtAccessTokenConverter jwtAccessTokenConverter
    ) {
      DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
      defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter);
      defaultTokenServices.setTokenStore(tokenStore);
      defaultTokenServices.setReuseRefreshToken(false);
      defaultTokenServices.setSupportRefreshToken(true);
      defaultTokenServices.setClientDetailsService(clientDetailsService);
      return defaultTokenServices;
    }
  }

  @Configuration
  @EnableWebSecurity
  public static class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {
      web.ignoring()
          .antMatchers("/styles/**")
          .antMatchers("/elements/**")
          .antMatchers("/images/**")
          .antMatchers("/bower_components/**")
          .antMatchers("/fonts/**")
          .antMatchers("/scripts/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
      http.csrf().ignoringAntMatchers("/oauth/token/revoke");
    }

    @Autowired
    void configureGlobal(AuthenticationManagerBuilder auth, AuthenticationProvider provider) {
      auth.authenticationProvider(provider);
    }
  }

  @Configuration
  @EnableResourceServer
  @RequiredArgsConstructor
  public static class OAuthResourceServer extends ResourceServerConfigurerAdapter {

    private final ApplicationProperties applicationProperties;

    @Override
    public void configure(HttpSecurity http) throws Exception {
      http
          .authorizeRequests()
          .antMatchers("/login").permitAll()
          .antMatchers("/login/passwordless/**")
              .access(applicationProperties.getAuth().getBiometric().getAccess())
          .antMatchers("/v1/admin/**").hasAnyRole("SUPER_ADMIN", "USER_MANAGER")
          .antMatchers("/v1/users/**").authenticated()
          .antMatchers("/oauth/token/revoke").authenticated()
          .anyRequest().authenticated()

          .and()
          .formLogin()
          .loginPage("/login")
          .defaultSuccessUrl("/")

          .and()
          .csrf().disable();
    }
  }
}
