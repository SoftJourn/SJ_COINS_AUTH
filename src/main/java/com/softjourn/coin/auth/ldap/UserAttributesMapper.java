package com.softjourn.coin.auth.ldap;

import com.softjourn.coin.auth.entity.User;
import java.util.Objects;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.stereotype.Component;

@Component
public class UserAttributesMapper implements AttributesMapper<User> {

  @Override
  public User mapFromAttributes(Attributes attributes) throws NamingException {
    User user = new User();

    Attribute fullName = attributes.get("cn");
    if (Objects.nonNull(fullName)) user.setFullName((String) fullName.get());

    Attribute email = attributes.get("mail");
    if (Objects.nonNull(email)) user.setEmail((String) email.get());

    Attribute ldapName = attributes.get("uid");
    if (Objects.nonNull(ldapName)) user.setLdapId((String) ldapName.get());

    return user;
  }
}
