package com.softjourn.coin.auth.service;

import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.exception.*;
import com.softjourn.coin.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.ConfigurationException;
import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminService {

    private UserRepository userRepository;
    private LdapService ldapService;


    @Autowired
    public AdminService(UserRepository userRepository, LdapService ldapService
            , @Value("${super.admins}") String[] superAdmins
            , @Value("${super.roles}") String[] superRoles) throws ConfigurationException {
        this.userRepository = userRepository;
        this.ldapService = ldapService;
        this.removeSuperUsers(superRoles);
        this.init(superAdmins,superRoles);
    }

    private void init(String[] superAdmins, String[] superRoles) throws ConfigurationException {

        if (superAdmins != null && superRoles != null && superAdmins.length > 0 && superRoles.length > 0) {
            Arrays.stream(superAdmins).forEach(u -> addSuperUser(u,superRoles));
        } else {
            throw new ConfigurationException("Please set up proper super.admins and super.roles");
        }

    }

    private void removeSuperUsers(String[] superRoles) throws ConfigurationException {
        if ( superRoles != null && superRoles.length > 0) {
            userRepository.delete(this.getSuperAdmins(superRoles[0]));
        } else {
            throw new ConfigurationException("Please set up proper super.roles");
        }
    }

    private List<User> getSuperAdmins(String superRole) {
        Role role = new Role(superRole,true);
        return userRepository.findByAuthorities(role);
    }

    private User addSuperUser(String ldapName,String[] superRoles) {
        User superUser = ldapService.getUser(ldapName);
        if (superUser != null) {
            //  Roles set up
            Set<Role> authorities = new HashSet<>();
            Arrays.stream(superRoles).forEach(r -> authorities.add(new Role(r,true)));
            superUser.setAuthorities(authorities);
        } else {
            throw new IllegalArgumentException("There is no user with name " + ldapName + " " +
                    "in LDAP database. Set correct LDAP name of superAdmin user.");
        }
        userRepository.save(superUser);
        return superUser;

    }

    boolean isAdmin(String login) {
        return userRepository.exists(login);
    }

    public List<User> getAdmins() {
        return userRepository.findAll();
    }

    public User find(String ldapId) {
        return userRepository.findOne(ldapId);
    }

    public User add(User user) {
        try {
            if (isValid(user)) {
                if (isAdmin(user))
                    throw new DuplicateEntryException(user);
                else
                    return userRepository.save(user);
            }
            else
                throw new IllegalAddException();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof EntityNotFoundException)
                throw new NotValidRoleException(user);
            throw e;
        }
    }

    private boolean isAdmin(User user) {
        return isAdmin(user.getLdapId());
    }

    private boolean isValid(User user) {
        return ldapService.userExist(user.getLdapId())
                &&!isSuper(user);
    }

    public void delete(String ldapName) {

        if (isAdmin(ldapName)) {
            if (isSuper(ldapName))
                throw new DeletingSuperUserException(userRepository.findOne(ldapName));
            userRepository.delete(ldapName);
        } else {
            throw new NoSuchUserException(ldapName);
        }
    }

    private boolean isSuper(String ldapName) {
        try {
            return this.isSuper(userRepository.findOne(ldapName));
        }catch (Exception e){
            return false;
        }
    }

    private boolean isSuper(User user) {
        try {
            Set<Role> authorities = user.getAuthorities();
            return authorities.stream().filter(Role::isSuperRole).count() > 0;
        }catch (Exception e){
            return false;
        }
    }


    public void updateAdmin(User updatedUser) {
        userRepository.save(updatedUser);
    }

    void delete(User testUser) {
        this.delete(testUser.getLdapId());
    }
}
