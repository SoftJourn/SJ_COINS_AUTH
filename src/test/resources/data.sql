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
