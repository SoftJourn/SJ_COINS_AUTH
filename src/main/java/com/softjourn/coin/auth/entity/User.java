package com.softjourn.coin.auth.entity;


import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Integer id;

    @Column
    @NotBlank
    private String ldapName;

    @Column
    @NotBlank
    private String fullName;

    @Column
    @NotBlank
    @Email
    private String email;
}
