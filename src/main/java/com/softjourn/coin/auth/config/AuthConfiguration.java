package com.softjourn.coin.auth.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.security.KeyPair;

@Configuration
public class AuthConfiguration {

    @Configuration
    @EnableAuthorizationServer
    public static class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

        private final AuthenticationProvider authenticationProvider;

        private final TokenStore tokenStore;

        private final DataSource dataSource;

        private final UserDetailsService userDetailsService;

        private final JwtAccessTokenConverter jwtAccessTokenConverter;

        private final ClientDetailsService clientDetailsService;

        @Autowired
        public AuthServerConfig(UserDetailsService userDetailsService
                , @SuppressWarnings("SpringJavaAutowiringInspection") DataSource dataSource
                , ClientDetailsService clientDetailsService, JwtAccessTokenConverter jwtAccessTokenConverter
                , AuthenticationProvider authenticationProvider, TokenStore tokenStore) {
            this.userDetailsService = userDetailsService;
            this.dataSource = dataSource;
            this.clientDetailsService = clientDetailsService;
            this.jwtAccessTokenConverter = jwtAccessTokenConverter;
            this.authenticationProvider = authenticationProvider;
            this.tokenStore = tokenStore;
        }

        @Bean
        public static JwtAccessTokenConverter jwtAccessTokenConverter(@Value("${authKeyFileName}") String authKeyFileName,
                                                                      @Value("${authKeyStorePass}") String authKeyStorePass,
                                                                      @Value("${authKeyMasterPass}") String authKeyMasterPass,
                                                                      @Value("${authKeyAlias}") String authKeyAlias) throws MalformedURLException {
            JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
            KeyPair keyPair = new KeyStoreKeyFactory(
                    new UrlResource("file:" + authKeyFileName), authKeyStorePass.toCharArray())
                    .getKeyPair(authKeyAlias, authKeyMasterPass.toCharArray());
            converter.setKeyPair(keyPair);
            return converter;
        }

        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {

        }

        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients
                    .jdbc(dataSource)
                    .passwordEncoder(new BCryptPasswordEncoder());
        }

        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints
                    .authenticationManager(authenticationProvider::authenticate)
                    .tokenStore(tokenStore)
                    .tokenServices(defaultTokenServices(jwtAccessTokenConverter))
                    .accessTokenConverter(jwtAccessTokenConverter)
                    .userDetailsService(userDetailsService);

        }

        @Bean
        @Primary
        @Qualifier(value = "consumerTokenServices")
        public DefaultTokenServices defaultTokenServices(JwtAccessTokenConverter jwtAccessTokenConverter) {
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
            web
                    .ignoring()
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
        void configureGlobal(AuthenticationManagerBuilder auth, AuthenticationProvider provider) throws Exception {
            auth.authenticationProvider(provider);
        }
    }

    @Configuration
    @EnableResourceServer
    public static class OAuthResourceServer extends ResourceServerConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .antMatchers("/login").permitAll()
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
