package com.softjourn.coin.auth.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @NotBlank
    private String ldapId;

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name="users_role", inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> authorities;

}
