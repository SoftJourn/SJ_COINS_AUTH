ALTER TABLE users DROP COLUMN ldap_name;
ALTER TABLE users DROP COLUMN full_name;
ALTER TABLE users DROP COLUMN authorities;

ALTER TABLE users CHANGE fullName full_name VARCHAR(255);
ALTER TABLE users CHANGE ldapName ldap_name VARCHAR(255);