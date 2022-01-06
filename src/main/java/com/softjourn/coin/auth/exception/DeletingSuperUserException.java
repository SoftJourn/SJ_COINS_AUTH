package com.softjourn.coin.auth.exception;

import com.softjourn.coin.auth.entity.User;

public class DeletingSuperUserException extends RuntimeException{

  public DeletingSuperUserException(User user){
    super(message(user.getLdapId()));
  }

  private static String  message(String userName){
    return "Your are trying to delete SUPER USER " + userName +" in runtime";
  }
}
