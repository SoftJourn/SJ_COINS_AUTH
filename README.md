# SJ Coin Auth

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


### Step 3: Add sensitive properties:

```bash
mkdir $HOME/.auth
touch application.properties
```

Add this properties to the previously created file

```properties
# Datasource
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/sj_auth?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false
spring.datasource.username=user
spring.datasource.password=somePassword

#Users
super.admins=someUsername

ldapServerURL=ldap://ldap.somehostname
ldapRoot=dc=ldap,dc=somedc
ldapUsersBase=ou=People,ou=Users

authKeyFileName=/home/someUserName/.auth/auth.jks
authKeyStorePass=keyStorePassword
authKeyMasterPass=keyStorePassword
authKeyAlias=auth

# Eris
eris.chain.url=http://someHostname:1337

# Biometric Auth
biometric.auth.client_id=biometric_app
# Access SpEL - e.g. "permitAll()", "hasIpAddress('X.X.X.X')"
biometric.auth.access=hasIpAddress('127.0.0.1')
```

### Step 4: Add logback configuration

```bash
cd $HOME/.auth
touch logback.xml
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

### Step 5: Run project and enter in browser [https://localhost:8111/login](https://localhost:8111/login)