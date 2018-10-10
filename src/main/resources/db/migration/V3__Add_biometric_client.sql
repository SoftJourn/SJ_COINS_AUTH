-- Add biometric auth client. Note: client_secret should be changed in production environment!
INSERT INTO `oauth_client_details` (`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`) VALUES
  ('biometric_app', NULL, 'supersecret', 'read,write', 'authorization_code,refresh_token', 'https://sjcoins.testing.softjourn.if.ua/vending/sso', 'ROLE_CLIENT', 6000000, 9000000, NULL, 'true');
