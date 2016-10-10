package com.softjourn.coin.auth.service;

import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.exception.DeletingSuperUserException;
import com.softjourn.coin.auth.exception.NoSuchUserException;
import com.softjourn.coin.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class AdminService {

    @Value("${super.admins}")
    private String[] superAdminLdapNames;

    private final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    private final String ADMIN = "ROLE_ADMIN";

    private UserRepository userRepository;
    private LdapService ldapService;

    @Autowired
    public AdminService(UserRepository userRepository, LdapService ldapService) {
        this.userRepository = userRepository;
        this.ldapService = ldapService;
    }

    @PostConstruct
    private void superUsersSetUp() {

        List<User> superAdminUsers=this.getSuperAdminUsers();
        if(superAdminUsers!=null) {
            superAdminUsers.forEach(user -> user.setAuthorities(ADMIN));
            userRepository.save(superAdminUsers);
        }
        Arrays.stream(superAdminLdapNames).forEach(this::addSuperUser);
    }

    private User addSuperUser(String ldapName) {

        User superUser = ldapService.getUser(ldapName);

        if (superUser != null) {
            superUser.setAuthorities(SUPER_ADMIN);
        } else {
            throw new IllegalArgumentException("There is no user with name " + ldapName + " " +
                    "in LDAP database. Set correct LDAP name of superAdmin user.");
        }
        userRepository.save(superUser);
        return superUser;

    }

    private List<User> getSuperAdminUsers() {
        return userRepository.findSuperAdminUsers();
    }

    public boolean isSuperAdmin(String ldapName){
        return userRepository.findSuperAdminUser(ldapName)!=null;
    }

    public boolean isAdmin(String login) {
        return userRepository.countByLdapName(login) > 0;
    }

    public List<User> getAdmins() {
        return userRepository.getAll();
    }

    public User getAdmin(String LdapId) {
        return userRepository.findOne(LdapId);
    }

    public void addAsAdmin(User user) {
        userRepository.save(user);
    }

    public void deleteFromAdmins(String ldapName) {

        if (isAdmin(ldapName)) {
            if (isSuperAdmin(ldapName))
                throw new DeletingSuperUserException(userRepository.findOne(ldapName));
            userRepository.delete(ldapName);
        } else {
            throw new NoSuchUserException(ldapName);
        }
    }

}
