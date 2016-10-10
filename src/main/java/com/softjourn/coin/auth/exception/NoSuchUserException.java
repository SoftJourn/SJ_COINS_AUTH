package com.softjourn.coin.auth.exception;


public class NoSuchUserException extends RuntimeException {
    public NoSuchUserException(String name){
        super("User "+name+" is not admin!");
    }
}
