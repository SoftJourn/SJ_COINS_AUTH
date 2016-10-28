package com.softjourn.coin.auth.exception;

import com.softjourn.coin.auth.entity.User;

public class NotValidRoleException extends RuntimeException{
    public NotValidRoleException(User user) {
        super(message(user.getLdapName()));
    }
    private static String  message(String userName){
        return "Your are trying to add user with not valid role "
                +userName
                +" in runtime";
    }

}
