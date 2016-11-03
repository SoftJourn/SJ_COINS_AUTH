package com.softjourn.coin.auth.config;


import com.softjourn.coin.auth.ldap.LdapAuthoritiesPopulationBean;
import com.softjourn.coin.auth.repository.TokenRepository;
import com.softjourn.coin.auth.repository.UserRepository;
import com.softjourn.coin.auth.service.LdapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.security.Principal;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@Configuration
@ComponentScan(basePackages = "com.softjourn.coin.auth.controller"
        , excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.softjourn.coin.auth.controller.AdminController.class))
@PropertySources({
        @PropertySource("classpath:security.properties")
})
@EnableWebMvc
public class AuthTestConfiguration {
    @Configuration
    @EnableAuthorizationServer
    public static class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

        @Autowired
        private TokenStore tokenStore;

        @Autowired
        private DataSource dataSource;

        @Autowired
        private UserDetailsService userDetailsService;


        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {

        }

        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients
                    .jdbc(dataSource)
                    .passwordEncoder(new BCryptPasswordEncoder());
        }

        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints
                    .authenticationManager(authenticationProvider()::authenticate)
                    .tokenStore(tokenStore)
                    .userDetailsService(userDetailsService);
        }

        @Bean
        public TokenRepository tokenRepository() {
            TokenRepository bean = mock(TokenRepository.class);

            when(bean.isTokenAlive(anyString())).thenReturn(true);
            when(bean.getAliveTokens()).thenReturn(Collections.singletonList("tokenValue"));

            return bean;
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return username -> new UserDetails() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return null;
                }

                @Override
                public String getPassword() {
                    return null;
                }

                @Override
                public String getUsername() {
                    return "user";
                }

                @Override
                public boolean isAccountNonExpired() {
                    return true;
                }

                @Override
                public boolean isAccountNonLocked() {
                    return true;
                }

                @Override
                public boolean isCredentialsNonExpired() {
                    return true;
                }

                @Override
                public boolean isEnabled() {
                    return true;
                }
            };
        }

        @Bean
        public LdapAuthoritiesPopulationBean ldapAuthoritiesPopulatorBean() {
            LdapAuthoritiesPopulationBean bean = mock(LdapAuthoritiesPopulationBean.class);
            List<? extends GrantedAuthority> authorities = new ArrayList<GrantedAuthority>() {{
                add((GrantedAuthority) () -> "ROLE_ADMIN");
                add((GrantedAuthority) () -> "ROLE_USER");
            }};
            when(bean.getGrantedAuthorities(any(), anyString())).then(i -> authorities);
            return bean;
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
            return new AuthenticationProvider() {
                @Override
                public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                    authentication = spy(authentication);
                    when(authentication.isAuthenticated()).thenReturn(true);
                    return authentication;
                }

                @Override
                public boolean supports(Class<?> authentication) {
                    return true;
                }
            };
        }

        @Bean
        public TokenStore tokenStore() {
            tokenStore = mock(TokenStore.class);
            when(tokenStore.readRefreshToken(anyString())).thenReturn(() -> "tokenValue");
            OAuth2Request request = new OAuth2Request(Collections.emptyMap(),
                    "client",
                    Collections.emptyList(),
                    true,
                    new HashSet<String>() {{
                        add("read");
                        add("write");
                    }},
                    Collections.emptySet(),
                    "www.redirect.url",
                    Collections.emptySet(),
                    Collections.emptyMap()
            );

            Authentication authentication = mock(Authentication.class);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("user");
            when(authentication.getPrincipal()).thenReturn((Principal) () -> "user");

            OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(request, authentication);
            when(tokenStore.readAuthenticationForRefreshToken(any())).thenReturn(oAuth2Authentication);

            return tokenStore;
        }

        @Bean
        public EmbeddedDatabase dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.HSQL)
                    .addScript("token.controller/schema.sql")
                    .addScript("token.controller/data.sql")
                    .build();
        }

        @Bean
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        public LdapService ldapservice() {
            return mock(LdapService.class);
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
            defaultTokenServices = spy(defaultTokenServices);
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
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .antMatchers("/api/**").permitAll()
                    .antMatchers("/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()

                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/")

                    .and()
                    .csrf()
                    .ignoringAntMatchers("/auth/token")
                    .ignoringAntMatchers("/api/**")
                    .ignoringAntMatchers("/admin/**");

        }

        @Autowired
        void configureGlobal(AuthenticationManagerBuilder auth, AuthenticationProvider provider) throws Exception {
            auth
                    .authenticationProvider(provider);
        }
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
