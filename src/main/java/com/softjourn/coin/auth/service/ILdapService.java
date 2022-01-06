package com.softjourn.coin.auth.service;

import com.softjourn.coin.auth.entity.User;
import java.util.List;

public interface ILdapService {

  List<User> getAllUsers();

  boolean userExist(User user);

  User getUser(String ldapId);
}
