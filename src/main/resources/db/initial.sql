CREATE TABLE IF NOT EXISTS oauth_client_details
(
  client_id VARCHAR(64) PRIMARY KEY NOT NULL,
  resource_ids VARCHAR(255),
  client_secret VARCHAR(255),
  scope VARCHAR(255),
  authorized_grant_types VARCHAR(255),
  web_server_redirect_uri VARCHAR(255),
  authorities VARCHAR(255),
  access_token_validity INT(11),
  refresh_token_validity INT(11),
  additional_information VARCHAR(4096),
  autoapprove VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users
(
  id       INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  ldapName VARCHAR(255),
  fullName VARCHAR(255),
  email    VARCHAR(255)
);

INSERT INTO oauth_client_details
(client_id, resource_ids, client_secret,
 scope, authorized_grant_types, web_server_redirect_uri,
 authorities, access_token_validity, refresh_token_validity,
 additional_information, autoapprove)
VALUES
  ('user_cred', NULL, 'supersecret',
                'read,write', 'password,refresh_token', NULL,
                'ROLE_CLIENT', NULL, NULL,
                NULL, 'true');

INSERT INTO oauth_client_details (client_id, resource_ids, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove)
VALUES
  ('vending_admin', NULL, 'supersecret', 'read,write', 'authorization_code,refresh_token', 'https://localhost:8222/sso',
                    'ROLE_CLIENT', NULL, NULL, NULL, 'true');

CREATE TABLE refresh_tokens
(
  id BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL,
  expiration DATETIME NOT NULL,
  value VARCHAR(1024) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
  ldap_id varchar(255) NOT NULL DEFAULT '',
  full_name varchar(255) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ldap_id`)
) DEFAULT CHARSET utf8;

CREATE TABLE IF NOT EXISTS role
(
  id VARCHAR(255) PRIMARY KEY NOT NULL,
  super_role TINYINT(1) DEFAULT 0 NOT NULL
) DEFAULT CHARSET utf8;

CREATE TABLE IF NOT EXISTS users_role
(
  user_ldap_id VARCHAR(255) NOT NULL,
  role_id VARCHAR(255) NOT NULL,
  CONSTRAINT users_role_users_ldap_id_fk FOREIGN KEY (user_ldap_id) REFERENCES users (ldap_id),
  CONSTRAINT users_role_role_id_fk FOREIGN KEY (role_id) REFERENCES role (id)
) DEFAULT CHARSET utf8;


INSERT INTO sj_auth.oauth_client_details (client_id, resource_ids, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove)
VALUES ('server_client', NULL,
                         'v-%kXyBTm%wf2T+JsPv_SjHP2d8*Fq#XYwE+m@pJM5EeZf?-^fXX&_$m34#e89H@8Ss=LQG^&^!!sXw^j#*MJSmuuP%_ZupfxYd@gxCxF%rTLy%wN%7ENtCa%BMD!ZXP',
                         'read', 'client_credentials', NULL, 'ROLE_APPLICATION', NULL, NULL, NULL, 'true');