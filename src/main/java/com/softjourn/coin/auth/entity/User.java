package com.softjourn.coin.auth.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @NotBlank
    private String ldapName;

    @NotBlank
    private String authorities;

    @NotBlank
    private String fullName;

    @Column
    @NotBlank
    @Email
    private String email;
}
