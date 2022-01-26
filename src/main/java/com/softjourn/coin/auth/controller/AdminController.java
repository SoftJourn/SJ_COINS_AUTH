package com.softjourn.coin.auth.controller;

import com.softjourn.coin.auth.entity.User;
import com.softjourn.coin.auth.service.AdminService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin")
public class AdminController {

  private final AdminService adminService;

  @GetMapping
  public List<User> getAll() {
    return adminService.getAdmins();
  }

  @PostMapping
  public User addNewAdmin(@RequestBody User user) {
    return adminService.add(user);
  }

  @PostMapping("/{name:.+}")
  public User updateAdmin(@RequestBody User user, @PathVariable String name) {
    return adminService.update(user, name);
  }

  @DeleteMapping("/{name:.+}")
  public void removeAdmin(@PathVariable String name) {
    adminService.delete(name);
  }
}
