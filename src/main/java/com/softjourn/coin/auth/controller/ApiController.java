package com.softjourn.coin.auth.controller;

import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.service.ILdapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
public class ApiController {

    private final ILdapService ldapService;

    @Autowired
    public ApiController(ILdapService ldapService) {
        this.ldapService = ldapService;
    }

    @RequestMapping(value = "/{ldapId:.+}", method = RequestMethod.GET)
    public User userExist(@PathVariable final String ldapId) {
        return ldapService.getUser(ldapId);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<User> getAll(){
        return ldapService.getAllUsers();
    }
}
