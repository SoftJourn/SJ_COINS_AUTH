package com.softjourn.coin.auth.repository;

import com.softjourn.coin.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
}
