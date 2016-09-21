package com.softjourn.coin.auth.controller;


import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.ldap.LdapService;
import com.softjourn.coin.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final LdapService ldapService;

    @Autowired
    public AdminController(LdapService ldapService, UserRepository userRepository) {
        this.ldapService = ldapService;
        this.userRepository = userRepository;
    }

    @RequestMapping()
    public List<User> getAll(){
        return   userRepository.getAll();
    }

    @RequestMapping(method = RequestMethod.POST )
    public User addNewAdmin(@RequestBody User user){
        if(ldapService.getAdmin(user.getLdapName())==null){
            User ldapUser=ldapService.getUser(user.getLdapName());
            if(ldapUser!=null){
                ldapUser.setAuthorities("ROLE_ADMIN");
                ldapService.addAsAdmin(ldapUser);
            }
            return ldapUser;
        }
        return null;
    }
    @RequestMapping(value = "/{name:.+}" ,method = RequestMethod.DELETE)
    public void removeAdmin(@PathVariable String name){
        ldapService.deleteFromAdmins(name);
    }
}
