package com.softjourn.coin.auth.service;


import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.ldap.UserAttributesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;


@Service
public class LdapService {

    @Value("${ldapUsersBase}")
    private String ldapUsersBase;

    private LdapTemplate ldapTemplate;

    @Autowired
    public LdapService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public List<User> getAllUsers() {
        List<User> result = ldapTemplate.search(ldapUsersBase, "(&(mail=*)(objectClass=person))", new UserAttributesMapper());
        result.sort(Comparator.comparing(User::getFullName));
        return result;
    }

    boolean userExist(User user) {
        User ldapUser = getUser(user.getLdapId());
        if (ldapUser == null)
            return false;
        User ldapAnalogFromRequest = new User(user.getLdapId(), user.getFullName()
                , user.getEmail(), null);
        return ldapUser.equals(ldapAnalogFromRequest);
    }

    public User getUser(String ldapId) {
        List<User> result = ldapTemplate.search(ldapUsersBase, "(uid=" + ldapId + ")", new UserAttributesMapper());
        return result.stream().findFirst().orElse(null);
    }
}