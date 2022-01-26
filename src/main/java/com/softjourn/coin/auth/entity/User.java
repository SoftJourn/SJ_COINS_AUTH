package com.softjourn.coin.auth.entity;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

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
  @JoinTable(name = "users_role", inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> authorities;
}
