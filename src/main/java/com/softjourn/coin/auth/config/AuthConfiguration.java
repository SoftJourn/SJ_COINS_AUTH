package com.softjourn.coin.auth.config;


import com.softjourn.coin.auth.ldap.LdapAuthoritiesPopulatorBean;
import com.softjourn.coin.auth.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
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
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.sql.DataSource;
import java.security.KeyPair;

@Configuration
public class AuthConfiguration extends SpringBootServletInitializer {
    @Configuration
    @EnableAuthorizationServer
    public static class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

        @Autowired
        private AuthenticationProvider authenticationProvider;

        @Autowired
        private JwtAccessTokenConverter jwtAccessTokenConverter;

        @Autowired
        private TokenStore tokenStore;

        @Autowired
        private DataSource dataSource;

        @Autowired
        private UserDetailsService userDetailsService;

        @Autowired
        private ClientDetailsService clientDetailsService;

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
                    .accessTokenConverter(jwtAccessTokenConverter)
                    .tokenStore(tokenStore)
                    .userDetailsService(userDetailsService);
        }

        @Bean
        @Autowired
        public TokenStore tokenStore(TokenRepository tokenRepository, JwtAccessTokenConverter jwtAccessTokenConverter) {
            return new RevocableJwtTokenStore(new JwtTokenStore(jwtAccessTokenConverter), tokenRepository);
        }

        @Bean
        public JwtAccessTokenConverter jwtAccessTokenConverter(@Value("${authKeyFileName}") String authKeyFileName,
                                                               @Value("${authKeyStorePass}") String authKeyStorePass,
                                                               @Value("${authKeyMasterPass}") String authKeyMasterPass,
                                                               @Value("${authKeyAlias}") String authKeyAlias) {
            JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
            KeyPair keyPair = new KeyStoreKeyFactory(
                    new ClassPathResource(authKeyFileName), authKeyStorePass.toCharArray())
                    .getKeyPair(authKeyAlias, authKeyMasterPass.toCharArray());
            converter.setKeyPair(keyPair);
            return converter;
        }

        @Bean
        public DefaultTokenServices tokenServices() {
            DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
            defaultTokenServices.setTokenStore(tokenStore);
            defaultTokenServices.setSupportRefreshToken(true);
            return defaultTokenServices;
        }
    }

    @Configuration
    @EnableWebSecurity
    public static class SecurityConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        LdapAuthoritiesPopulatorBean ldapAuthoritiesPopulatorBean;

        @Value("${ldapServerURL}")
        String ldapURL;
        @Value("${ldapRoot}")
        String ldapRoot;
        @Value("${ldapUsersBase}")
        String ldapUsersBase;

        @Bean
        @Autowired
        LdapTemplate getLdapTemplate(LdapContextSource ldapContextSource) {
            LdapTemplate template = new LdapTemplate(ldapContextSource);
            template.setIgnorePartialResultException(true);

            return template;
        }

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
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .antMatchers("/oauth/token/revoke").permitAll()
                    .antMatchers("/api/**").permitAll()
                    .antMatchers("/admin/**").permitAll()
                    .anyRequest().authenticated()

                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/")

                    .and()
                    .csrf()
                    .ignoringAntMatchers("/oauth/**")
                    .ignoringAntMatchers("/api/**")
                    .ignoringAntMatchers("/admin/**");

        }

        @Autowired
        void configureGlobal(AuthenticationManagerBuilder auth, AuthenticationProvider provider) throws Exception {
            auth.authenticationProvider(provider);
        }
    }
}
