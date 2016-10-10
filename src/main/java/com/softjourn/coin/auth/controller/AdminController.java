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
    public List<User> getAll(){
        return   adminService.getAdmins();
    }

    @RequestMapping(method = RequestMethod.POST )
    public ResponseEntity<User> addNewAdmin(@RequestBody User user){

        if(adminService.getAdmin(user.getLdapName())==null){
            User ldapUser=ldapService.getUser(user.getLdapName());
            if(ldapUser!=null){
                ldapUser.setAuthorities("ROLE_ADMIN");
                adminService.addAsAdmin(ldapUser);
                return new ResponseEntity<>(ldapUser, HttpStatus.OK);
            }else {
                throw new NoSuchLdapNameException(user);
            }

        }else {
            throw new DuplicateEntryException(user);
        }
    }

    @RequestMapping(value = "/{name:.+}" ,method = RequestMethod.DELETE)
    public void removeAdmin(@PathVariable String name){
        adminService.deleteFromAdmins(name);
    }
}
