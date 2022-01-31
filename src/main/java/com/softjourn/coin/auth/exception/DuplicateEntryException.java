package com.softjourn.coin.auth.exception;

import com.softjourn.coin.auth.entity.User;

public class DuplicateEntryException extends RuntimeException{

  public DuplicateEntryException(User user) {
    super(message(user.getLdapId()));
  }

  private static String  message(String name){
    return "Duplicate entry. User " + name + " is admin";
  }
}
