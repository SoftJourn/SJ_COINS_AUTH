package com.softjourn.coin.auth.exception;

import com.softjourn.coin.auth.entity.User;


public class NotValidUserException extends RuntimeException {
    public NotValidUserException(User user) {
        super("User "+user.getLdapName()+" is not Valid");
    }
}
