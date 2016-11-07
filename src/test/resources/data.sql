DROP TABLE IF EXISTS oauth_client_details;

CREATE TABLE oauth_client_details
(
  client_id VARCHAR(64) PRIMARY KEY NOT NULL,
  resource_ids VARCHAR(256),
  client_secret VARCHAR(256),
  scope VARCHAR(256),
  authorized_grant_types VARCHAR(256),
  web_server_redirect_uri VARCHAR(256),
  authorities VARCHAR(256),
  access_token_validity INT,
  refresh_token_validity INT,
  additional_information VARCHAR(4096),
  autoapprove VARCHAR(256)
);

INSERT INTO oauth_client_details
(client_id, resource_ids, client_secret,
 scope, authorized_grant_types, web_server_redirect_uri,
 authorities, access_token_validity, refresh_token_validity,
 additional_information, autoapprove)
VALUES
  ('client', NULL, 'secret',
                'read,write', 'password,refresh_token,authorization_code,implicit', 'client.redirect.uri',
                'ROLE_CLIENT', NULL, NULL,
                NULL, 'true');
