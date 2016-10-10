package com.softjourn.coin.auth.repository;


import com.softjourn.coin.auth.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends CrudRepository<User, String> {

    String SUPER_ROLE = "ROLE_SUPER_ADMIN";

    int countByLdapName(String login);

    @Query(value = "SELECT u FROM User u ")
    List<User> getAll();

    @Query(value = "SELECT u FROM User u WHERE u.authorities=com.softjourn.coin.auth.repository.UserRepository.SUPER_ROLE")
    List<User> findSuperAdminUsers();

    @Query(value = "SELECT u FROM User u WHERE u.ldapName=:user AND u.authorities=com.softjourn.coin.auth.repository.UserRepository.SUPER_ROLE")
    User findSuperAdminUser(@Param("user") String user);

}
