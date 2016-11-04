CREATE TABLE IF NOT EXISTS oauth_client_details
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

CREATE TABLE IF NOT EXISTS users
(
  id  INT PRIMARY KEY NOT NULL ,
  ldap_id VARCHAR(255),
  full_name VARCHAR(255),
  email    VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id BIGINT  PRIMARY KEY NOT NULL,
    value VARCHAR(1024),
    expiration DATETIME
);
