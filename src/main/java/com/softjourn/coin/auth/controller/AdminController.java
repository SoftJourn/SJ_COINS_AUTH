package com.softjourn.coin.auth.controller;


import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.exception.DuplicateEntryException;
import com.softjourn.coin.auth.exception.NoSuchLdapNameException;
import com.softjourn.coin.auth.service.AdminService;
import com.softjourn.coin.auth.service.LdapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final LdapService ldapService;

    @Autowired
    public AdminController(AdminService adminService, LdapService ldapService) {
        this.adminService = adminService;
        this.ldapService = ldapService;
    }

    @RequestMapping()
    public List<User> getAll() {
        return adminService.getAdmins();
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<User> addNewAdmin(@RequestBody User user) {

        User ldapUser = ldapService.getUser(user.getLdapId());
        if (ldapUser != null) {
            if (user.getAuthorities() == null || user.getAuthorities().isEmpty())
                throw new IllegalArgumentException();
            if (adminService.find(user.getLdapId()) == null) {
                //Implement restriction of user roles
                ldapUser.setAuthorities(user.getAuthorities());
                adminService.add(ldapUser);
                return new ResponseEntity<>(ldapUser, HttpStatus.OK);
            } else {
                throw new DuplicateEntryException(user);
            }

        } else {
            throw new NoSuchLdapNameException(user);
        }

    }

    @RequestMapping(value = "/{name:.+}", method = RequestMethod.POST)
    public ResponseEntity<User> updateAdmin(@RequestBody User user, @PathVariable String name) {
        User updateAdmin = adminService.find(name);
        if (updateAdmin != null) {
            if (user.getAuthorities().isEmpty())
                throw new IllegalArgumentException();
            updateAdmin.setAuthorities(user.getAuthorities());
            adminService.updateAdmin(updateAdmin);
            return new ResponseEntity<>(updateAdmin, HttpStatus.OK);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @RequestMapping(value = "/{name:.+}", method = RequestMethod.DELETE)
    public void removeAdmin(@PathVariable String name) {
        adminService.delete(name);
    }
}
