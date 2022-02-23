package com.softjourn.coin.auth;

import com.softjourn.common.spring.aspects.logging.EnableLoggingAspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("com.softjourn.coin.auth.repository")
@EnableLoggingAspect
@EntityScan("com.softjourn.coin.auth.entity")
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
