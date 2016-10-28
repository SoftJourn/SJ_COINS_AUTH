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

CREATE TABLE users
(
  id       INT PRIMARY KEY NOT NULL IDENTITY ,
  ldapName VARCHAR(256),
  fullName VARCHAR(256),
  email    VARCHAR(256)
);

CREATE TABLE refresh_tokens
(
    value VARCHAR(1024) PRIMARY KEY NOT NULL,
    expiration DATETIME
);
