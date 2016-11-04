package com.softjourn.coin.auth.ldap;


import com.softjourn.coin.auth.entity.User;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

@Component
public class UserAttributesMapper implements AttributesMapper<User> {

    @Override
    public User mapFromAttributes(Attributes attributes) throws NamingException {
        User user = new User();

        Attribute fullName = attributes.get("cn");
        if(fullName != null) user.setFullName((String) fullName.get());

        Attribute email = attributes.get("mail");
        if(email != null) user.setEmail((String) email.get());

        Attribute ldapName = attributes.get("uid");
        if(ldapName != null) user.setLdapName((String) ldapName.get());

        return user;
    }

}
