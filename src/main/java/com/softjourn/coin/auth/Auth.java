package com.softjourn.coin.auth;


import com.softjourn.coin.auth.config.LdapConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.softjourn.coin.auth")
@EnableJpaRepositories("com.softjourn.coin.auth.repository")
@Import({LdapConfiguration.class})
@SpringBootApplication
@PropertySources({
        @PropertySource("classpath:security.properties")
})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableResourceServer
public class Auth {

    public static void main(String[] args) {
        SpringApplication.run(Auth.class, args);
    }

}
