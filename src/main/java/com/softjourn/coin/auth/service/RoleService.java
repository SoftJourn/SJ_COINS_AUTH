package com.softjourn.coin.auth.service;

import com.softjourn.coin.auth.config.ApplicationProperties;
import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.exception.IllegalAddException;
import com.softjourn.coin.auth.repository.RoleRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

  private final RoleRepository roleRepository;

  @Autowired
  public RoleService(RoleRepository roleRepository, ApplicationProperties applicationProperties) {
    this.roleRepository = roleRepository;
    this.initSuperRoles(applicationProperties.getRole().getSuperRoles());
    this.initCommonRoles(applicationProperties.getRole().getRegularRoles());
  }

  private void initCommonRoles(String[] commonRoles) {
    Arrays.stream(commonRoles).forEach(commonRole -> this.add(new Role(commonRole)));
  }

  private void initSuperRoles(String[] superRoles) {
    Arrays.stream(superRoles).forEach(superRole -> this.addSuperRole(new Role(superRole, true)));
  }

  public Role add(String roleName) {
    try {
      return roleRepository.save(new Role(roleName));
    } catch (DataIntegrityViolationException e) {
      return roleRepository.getById(roleName);
    } catch (Exception e) {
      return null;
    }
  }

  public Role add(Role role) {
    if (role.isSuperRole()) {
      throw new IllegalAddException();
    }
    return this.add(role.getAuthority());
  }

  private Role addSuperRole(Role role) {
    if (!role.isSuperRole()) {
      throw new IllegalArgumentException();
    }
    return roleRepository.save(role);
  }

  public void removeRole(Role role) {
    if (role.isSuperRole()) {
      throw new IllegalArgumentException(
          "Role " + role.getAuthority() + " can't be deleted due to it is Super");
    }
    this.removeRole(role.getAuthority());
  }

  public void removeRole(String roleName) {
    Role role = roleRepository.getById(roleName);
    if (role.isSuperRole()) {
      throw new IllegalArgumentException(
          "Role " + role.getAuthority() + " can't be deleted due to it is Super");
    }
    roleRepository.delete(role);
  }

  public Role get(String roleName) {
    return roleRepository.getById(roleName);
  }

  public List<Role> getAll() {
    return roleRepository.findAll();
  }
}
