package com.softjourn.coin.auth.exception;

import com.softjourn.coin.auth.entity.User;

public class NoSuchLdapNameException extends RuntimeException{
    public NoSuchLdapNameException(User user) {
        super(message(user.getLdapName()));
    }
    private static String  message(String name){
        return "User "+name+" not in LDAP database";
    }
}
