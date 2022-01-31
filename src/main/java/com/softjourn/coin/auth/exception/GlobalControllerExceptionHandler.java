package com.softjourn.coin.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

  //TODO change to exception pattern like in vending server. ErrorDetail class
  @ExceptionHandler(DeletingSuperUserException.class)
  public ResponseEntity<String> deletingSuperUserRequest(DeletingSuperUserException e) {
    return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(NoSuchUserException.class)
  public ResponseEntity<String> deletingNotExistingUser(NoSuchUserException e) {
    return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(DuplicateEntryException.class)
  public ResponseEntity<String> duplicatingAdminEntity(DuplicateEntryException e) {
    return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
  }

  @ExceptionHandler(LDAPNotFoundException.class)
  public ResponseEntity<String> ldapNameDoesNotExists(LDAPNotFoundException e) {
    return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> illegalArgument(IllegalArgumentException e) {
    return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotValidRoleException.class)
  public ResponseEntity<String> thereIsNoSuchRoleInDB(NotValidRoleException e) {
    return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
  }
}
