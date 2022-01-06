package com.softjourn.coin.auth.repository;

import com.softjourn.coin.auth.entity.Role;
import com.softjourn.coin.auth.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

  List<User> findByAuthorities(Role role);
}
