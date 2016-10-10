package com.softjourn.coin.auth.service;


import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.ldap.UserAttributesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
        Collections.sort(result, (o1, o2) -> o1.getFullName().compareTo(o2.getFullName()));
        return result;
    }

    public boolean userExist(String ldapId) {
        return getUser(ldapId) != null;
    }

    public User getUser(String ldapId) {
        List<User> result = ldapTemplate.search(ldapUsersBase, "(uid=" + ldapId + ")", new UserAttributesMapper());
        return result.stream().findFirst().orElse(null);
    }
}

