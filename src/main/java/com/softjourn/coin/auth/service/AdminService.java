package com.softjourn.coin.auth.service;

import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.exception.*;
import com.softjourn.coin.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.naming.ConfigurationException;
import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// RoleService should be created before AdminService as this service using roles.
@DependsOn("roleService")
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
        this.init(superAdmins, superRoles);
    }

    private void init(String[] superAdmins, String[] superRoles) throws ConfigurationException {

        if (superAdmins != null && superRoles != null && superAdmins.length > 0 && superRoles.length > 0) {
            Arrays.stream(superAdmins).forEach(u -> addSuperUser(u, superRoles));
        } else {
            throw new ConfigurationException("Please set up proper super.admins and super.roles");
        }

    }

    private void removeSuperUsers(String[] superRoles) throws ConfigurationException {
        if (superRoles != null && superRoles.length > 0) {
            userRepository.delete(this.getSuperAdmins(superRoles[0]));
        } else {
            throw new ConfigurationException("Please set up proper super.roles");
        }
    }

    private List<User> getSuperAdmins(String superRole) {
        Role role = new Role(superRole, true);
        return userRepository.findByAuthorities(role);
    }

    private User addSuperUser(String ldapName, String[] superRoles) {
        User superUser = ldapService.getUser(ldapName);
        if (superUser != null) {
            //  Roles set up
            Set<Role> authorities = new HashSet<>();
            Arrays.stream(superRoles).forEach(r -> authorities.add(new Role(r, true)));
            superUser.setAuthorities(authorities);
        } else {
            throw new IllegalArgumentException("There is no user with name " + ldapName + " " +
                    "in LDAP database. Set correct LDAP name of superAdmin user.");
        }
        userRepository.save(superUser);
        return superUser;

    }

    boolean isAdmin(String ldapId) {
        return userRepository.exists(ldapId);
    }

    public List<User> getAdmins() {
        return userRepository.findAll();
    }

    public User find(String ldapId) {
        return userRepository.findOne(ldapId);
    }

    /**
     * Add new regular admin
     * Conditions:
     * 1. User exists in LDAP database
     * 2. User data from @param and LDAP should match exactly
     * 3. Authorities are not empty in @param
     * 4. User should not be admin
     * 5. User should not have super role due to they are not allowed to be assigned in this method
     *
     * @param user
     * @return user if conditions are sustained else exceptions
     */
    public User add(User user) {
        try {

            if (!ldapService.userExist(user))
                throw new LDAPNotFoundException("Wrong user data");
            if (user.getAuthorities() == null || user.getAuthorities().isEmpty())
                throw new IllegalArgumentException("Authorities are empty");
            if (isAdmin(user))
                throw new DuplicateEntryException(user);
            if (isSuper(user))
                throw new IllegalArgumentException("Super role can not be granted");
            return userRepository.save(user);

        } catch (RuntimeException e) {
            if (e.getCause() instanceof EntityNotFoundException)
                throw new NotValidRoleException(user);
            throw e;
        }
    }

    public User update(User user) {
        try {
            if (!ldapService.userExist(user))
                throw new LDAPNotFoundException("Wrong user data");
            if (user.getAuthorities() == null || user.getAuthorities().isEmpty())
                throw new IllegalArgumentException("Authorities are empty");
            if (!isAdmin(user))
                throw new NoSuchUserException(user.getLdapId());
            if (isSuper(user.getLdapId()) || isSuper(user))
                throw new IllegalArgumentException("Super role can not be changed or granted");
            //TODO find out why userRepository.save calls remove method from setAuthority HashSet
            //TEST method update_testUserWithWrongName_LDAPNotFoundException
            user.setAuthorities(new HashSet<>(user.getAuthorities()));
            userRepository.save(user);
            return user;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof EntityNotFoundException)
                throw new NotValidRoleException(user);
            throw e;
        }

    }

    public User update(User user, String name) {
        if (user.getLdapId() != null && user.getLdapId().equals(name))
            return update(user);
        else
            throw new IllegalArgumentException("Name in url does not match user in body");
    }

    private boolean isAdmin(User user) {
        return isAdmin(user.getLdapId());
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
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSuper(User user) {
        try {
            Set<Role> authorities = user.getAuthorities();
            return authorities.stream().filter(Role::isSuperRole).count() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    void delete(User testUser) {
        this.delete(testUser.getLdapId());
    }
}
