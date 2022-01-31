package com.softjourn.coin.auth.controller;

import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.service.ILdapService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class ApiController {

  private final ILdapService ldapService;

  @GetMapping("/{ldapId:.+}")
  public User userExist(@PathVariable final String ldapId) {
    return ldapService.getUser(ldapId);
  }

  @GetMapping
  public List<User> getAll(){
    return ldapService.getAllUsers();
  }
}
