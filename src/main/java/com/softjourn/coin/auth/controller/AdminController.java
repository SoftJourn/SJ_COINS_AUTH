package com.softjourn.coin.auth.controller;


import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @RequestMapping()
    public List<User> getAll() {
        return adminService.getAdmins();
    }

    @RequestMapping(method = RequestMethod.POST)
    public User addNewAdmin(@RequestBody User user) {
        return adminService.addNewAdmin(user);
    }

    @RequestMapping(value = "/{name:.+}", method = RequestMethod.POST)
    public User updateAdmin(@RequestBody User user, @PathVariable String name) {
        return adminService.updateAdmin(user, name);
    }

    @RequestMapping(value = "/{name:.+}", method = RequestMethod.DELETE)
    public void removeAdmin(@PathVariable String name) {
        adminService.delete(name);
    }
}
