package com.softjourn.coin.auth.config;


import com.softjourn.coin.auth.ldap.LdapAuthoritiesPopulatorBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
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
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;
import java.security.KeyPair;

@Configuration
@EnableWebMvc
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.softjourn.coin.auth")
@EnableJpaRepositories("com.softjourn.coin.auth.repository")
@EntityScan(basePackages = "com.softjourn.coin.auth.entity")
@Import({LdapConfiguration.class})
@SpringBootApplication
@PropertySources({
        @PropertySource("classpath:security.properties")
})

public class ServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Configuration
    public static class AuthManagerConfig extends GlobalAuthenticationConfigurerAdapter {

        @Autowired
        private UserDetailsService userDetailsService;

        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userDetailsService);
            super.init(auth);
        }

    }

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
                    .tokenStore(tokenStore);
        }

        @Bean
        public TokenStore tokenStore() {
            return new JwtTokenStore(jwtAccessTokenConverter);
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
        @Primary
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
                    .antMatchers("/login").permitAll()
                    .antMatchers("/api/**").permitAll()
                    .antMatchers("/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()

                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/")

                    .and()
                    .csrf()
                    .ignoringAntMatchers("/api/**")
                    .ignoringAntMatchers("/admin/**");

        }

        @Autowired
        void configureGlobal(AuthenticationManagerBuilder auth, AuthenticationProvider provider) throws Exception {
            auth.authenticationProvider(provider);
        }
    }
}
