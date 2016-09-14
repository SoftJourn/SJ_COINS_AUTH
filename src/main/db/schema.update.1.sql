CREATE TABLE refresh_tokens
(
    value VARCHAR(1024) PRIMARY KEY NOT NULL,
    expiration DATETIME
);