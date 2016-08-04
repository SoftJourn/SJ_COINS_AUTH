package com.softjourn.coin.auth.controller;

import com.softjourn.coin.auth.ldap.LdapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class ApiController {

    @Autowired
    private LdapService ldapService;

    @RequestMapping(value = "/{ldapId}/exist", method = RequestMethod.GET)
    public boolean userExist(@PathVariable final String ldapId) {
        return ldapService.userExist(ldapId);
    }
}
