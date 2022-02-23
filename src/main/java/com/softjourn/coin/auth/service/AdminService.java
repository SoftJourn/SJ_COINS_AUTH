package com.softjourn.coin.auth.service;

import com.softjourn.coin.auth.config.ApplicationProperties;
import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.exception.DeletingSuperUserException;
import com.softjourn.coin.auth.exception.DuplicateEntryException;
import com.softjourn.coin.auth.exception.LDAPNotFoundException;
import com.softjourn.coin.auth.exception.NoSuchUserException;
import com.softjourn.coin.auth.exception.NotValidRoleException;
import com.softjourn.coin.auth.repository.UserRepository;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.naming.ConfigurationException;
import javax.persistence.EntityNotFoundException;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

// RoleService should be created before AdminService as this service using roles.
@Service
@DependsOn("roleService")
public class AdminService {

  private final UserRepository userRepository;
  private final ILdapService ldapService;

  public AdminService(
      UserRepository userRepository,
      ILdapService ldapService,
      ApplicationProperties applicationProperties
  ) throws ConfigurationException {
    this.userRepository = userRepository;
    this.ldapService = ldapService;
    this.removeSuperUsers(applicationProperties.getRole().getSuperRoles());
    this.init(
        applicationProperties.getRole().getAdmins(),
        applicationProperties.getRole().getSuperRoles());
  }

  private void init(String[] superAdmins, String[] superRoles) throws ConfigurationException {

    if (Objects.nonNull(superAdmins) && Objects.nonNull(superRoles)
        && superAdmins.length > 0 && superRoles.length > 0) {
      Arrays.stream(superAdmins).forEach(u -> addSuperUser(u, superRoles));
    } else {
      throw new ConfigurationException("Please set up proper super.admins and super.roles");
    }
  }

  private void removeSuperUsers(String[] superRoles) throws ConfigurationException {
    if (Objects.nonNull(superRoles) && superRoles.length > 0) {
      userRepository.deleteAll(getSuperAdmins(superRoles[0]));
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
    if (Objects.nonNull(superUser)) {
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
    return getUserById(ldapId) != null;
  }

  public List<User> getAdmins() {
    return userRepository.findAll();
  }

  public User find(String ldapId) {
    return userRepository.findById(ldapId).orElse(null);
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
      if (Objects.isNull(user.getAuthorities()) || user.getAuthorities().isEmpty())
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
      if (Objects.isNull(user.getAuthorities()) || user.getAuthorities().isEmpty())
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
    if (Objects.nonNull(user.getLdapId()) && user.getLdapId().equals(name)) {
      return update(user);
    } else {
      throw new IllegalArgumentException("Name in url does not match user in body");
    }
  }

  private boolean isAdmin(User user) {
    return isAdmin(user.getLdapId());
  }

  public void delete(String ldapName) {
    if (isAdmin(ldapName)) {
      if (isSuper(ldapName)) {
        throw new DeletingSuperUserException(getUserById(ldapName));
      }
      userRepository.deleteById(ldapName);
    } else {
      throw new NoSuchUserException(ldapName);
    }
  }

  private boolean isSuper(String ldapName) {
    try {
      return isSuper(getUserById(ldapName));
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isSuper(User user) {
    try {
      return user.getAuthorities().stream().anyMatch(Role::isSuperRole);
    } catch (Exception e) {
      return false;
    }
  }

  void delete(User testUser) {
    delete(testUser.getLdapId());
  }

  private User getUserById(String ldapId) {
    return userRepository.getById(ldapId);
  }
}
