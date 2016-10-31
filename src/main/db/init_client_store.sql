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