CREATE TABLE refresh_tokens
(
  id         BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
  expiration DATETIME                          NOT NULL,
  value      VARCHAR(1024)                     NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
  ldap_id   VARCHAR(255) NOT NULL DEFAULT '',
  full_name VARCHAR(255)          DEFAULT NULL,
  email     VARCHAR(255)          DEFAULT NULL,
  PRIMARY KEY (`ldap_id`)
);

CREATE TABLE IF NOT EXISTS role
(
  id         VARCHAR(255) PRIMARY KEY NOT NULL,
  super_role BOOLEAN DEFAULT 0        NOT NULL
);

CREATE TABLE IF NOT EXISTS users_role
(
  user_ldap_id VARCHAR(255) NOT NULL,
  role_id      VARCHAR(255) NOT NULL,
  CONSTRAINT users_role_users_ldap_id_fk FOREIGN KEY (user_ldap_id) REFERENCES users (ldap_id),
  CONSTRAINT users_role_role_id_fk FOREIGN KEY (role_id) REFERENCES role (id)
);
