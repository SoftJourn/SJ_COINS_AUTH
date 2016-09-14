package com.softjourn.coin.auth.ldap;


import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
public class LdapService {

    @Value("${superAdminLdapName}")
    private String superAdminLdapName;

    @Value("${ldapUsersBase}")
    private String ldapUsersBase;

    private LdapTemplate ldapTemplate;

    private UserRepository userRepository;

    @Autowired
    public LdapService(LdapTemplate ldapTemplate, UserRepository userRepository) {
        this.ldapTemplate = ldapTemplate;
        this.userRepository = userRepository;
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

    private User getSuperAdminUser() {
        List<User> result = ldapTemplate.search(ldapUsersBase, "(uid=" + superAdminLdapName + ")", new UserAttributesMapper());
        if(result.isEmpty())
            throw new IllegalArgumentException("There is no user with name " + superAdminLdapName + " " +
                    "in LDAP database. Set correct LDAP name of superAdmin user.");
        return result.get(0);
    }

    public boolean isAdmin(String login) {
        return userRepository.countByLdapName(login) > 0;
    }

    public Iterable<User> getAdmins() {
        List<User> admins =  userRepository.getAll();
        admins.add(getSuperAdminUser());
        return admins;
    }

    public void addAsAdmin(User user) {
        userRepository.save(user);
    }

    public void deleteFromAdmins(Integer id) {
        userRepository.delete(id);
    }
}

