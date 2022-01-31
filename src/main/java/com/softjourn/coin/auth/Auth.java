package com.softjourn.coin.auth;

import com.softjourn.coin.auth.config.LdapConfiguration;
import com.softjourn.common.spring.aspects.logging.EnableLoggingAspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.softjourn.coin.auth")
@EnableJpaRepositories("com.softjourn.coin.auth.repository")
@EnableLoggingAspect
@EntityScan(basePackages = "com.softjourn.coin.auth.entity")
@Import({LdapConfiguration.class})
@SpringBootApplication
@PropertySources({
    @PropertySource(
        value = "file:${user.home}/.auth/application.properties", ignoreResourceNotFound = true)
})
public class Auth extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(Auth.class, args);
  }
}
