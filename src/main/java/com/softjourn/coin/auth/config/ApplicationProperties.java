package com.softjourn.coin.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("application")
public class ApplicationProperties {

  private Auth auth;
  private Ldap ldap;
  private Role role;

  @Getter
  @Setter
  public static class Auth {

    private String keyFileName;
    private String keyStorePass;
    private String keyMasterPass;
    private String keyAlias;
    private Biometric biometric;

    @Getter
    @Setter
    public static class Biometric {

      private String clientId;
      private String access;
    }
  }

  @Getter
  @Setter
  public static class Ldap {

    private String serverUrl;
    private String root;
    private String usersBase;
  }

  @Getter
  @Setter
  public static class Role {

    private String[] regularRoles;
    private String[] superRoles;
    private String[] admins;
  }
}
