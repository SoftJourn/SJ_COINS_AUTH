ALTER TABLE users CHANGE ldap_name ldap_id VARCHAR(255);
ALTER TABLE users_role CHANGE ldap_name user_ldap_id VARCHAR(255);