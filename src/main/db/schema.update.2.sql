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
