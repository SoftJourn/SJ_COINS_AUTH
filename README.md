# SJ Coin Auth

## Prepare stage
#### Setup environment variables.
```
SJ_COINS_AUTH_SERVER_PORT=8081
SJ_COINS_AUTH_SERVER_LOGGING_CONFIG=file:/path/to/logback.xml
SJ_COINS_AUTH_SERVER_ERIS_CHAIN_URL=http://localhost:1337
SJ_COINS_AUTH_SERVER_DATASOURCE_URL=jdbc:mysql://localhost:3306/sj_auth?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false
SJ_COINS_AUTH_SERVER_DATASOURCE_USER=sj
SJ_COINS_AUTH_SERVER_DATASOURCE_PASS=password
SJ_COINS_AUTH_SERVER_ADMINS=username
SJ_COINS_AUTH_SERVER_LDAP_URL=ldaps://ldap.softjourn.if.ua
SJ_COINS_AUTH_SERVER_LDAP_ROOT=dc=ldap,dc=sjua
SJ_COINS_AUTH_SERVER_LDAP_BASE=ou=People,ou=Users
SJ_COINS_AUTH_SERVER_AUTH_KEY_FILE=/path/to/config/auth/auth.jks
SJ_COINS_AUTH_SERVER_AUTH_STORE_PASS=password
SJ_COINS_AUTH_SERVER_AUTH_MASTER_PASS=password
```
Or with export:
```
export SJ_COINS_AUTH_SERVER_PORT=8081
export SJ_COINS_AUTH_SERVER_LOGGING_CONFIG='file:/path/to/logback.xml'
export SJ_COINS_AUTH_SERVER_ERIS_CHAIN_URL='http://localhost:1337'
export SJ_COINS_AUTH_SERVER_DATASOURCE_URL='jdbc:mysql://localhost:3306/sj_auth?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false'
export SJ_COINS_AUTH_SERVER_DATASOURCE_USER='sj'
export SJ_COINS_AUTH_SERVER_DATASOURCE_PASS='password'
export SJ_COINS_AUTH_SERVER_ADMINS='username'
export SJ_COINS_AUTH_SERVER_LDAP_URL='ldaps://ldap.softjourn.if.ua'
export SJ_COINS_AUTH_SERVER_LDAP_ROOT='dc=ldap,dc=sjua'
export SJ_COINS_AUTH_SERVER_LDAP_BASE='ou=People,ou=Users'
export SJ_COINS_AUTH_SERVER_AUTH_KEY_FILE='/path/to/config/auth/auth.jks'
export SJ_COINS_AUTH_SERVER_AUTH_STORE_PASS='password'
export SJ_COINS_AUTH_SERVER_AUTH_MASTER_PASS='password'
```

## Start up documentation

### Step 1: Create databases structure

#### Enter as root user and create user for these databases using commands:

```sql
CREATE USER 'user'@'localhost' IDENTIFIED BY 'somePassword';

GRANT ALL PRIVILEGES ON *.* TO 'user'@'localhost';
```

#### Enter as this new user and create databases:

```sql
CREATE DATABASE sj_auth CHARACTER SET utf8;
```

#### NOTE: All the tables will be created during the first service start.

### Step 2: Create keystore file, certificate and extract public key:

```bash
keytool -genkey -v -keystore auth.jks -alias auth -keyalg RSA -keysize 2048 -validity 10000

keytool -export -keystore auth.jks -alias auth -file auth.cer

openssl x509 -inform der -pubkey -noout -in auth.cer > auth.pub
```

### Step 3: Add logback configuration

```bash
touch /path/to/logback.xml
```

Add basic configuration to the file

```xml
<configuration debug="true" scan="true" scanPeriod="30">

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%-35(%d{dd-MM-yyyy} %magenta(%d{HH:mm:ss}) [%5.10(%thread)]) %highlight(%-5level) %cyan(%logger{16}) - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="info">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

### Step 5: Run project and enter in browser [https://localhost:8081/login](https://localhost:8081/login)