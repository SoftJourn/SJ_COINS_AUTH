package com.softjourn.coin.auth.entity;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class Token {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "value", length = 1024)
  private String tokenValue;

  @Column(name = "expiration")
  private Instant expirationTime;

  public Token(String tokenValue, Instant expirationTime) {
    this.tokenValue = tokenValue;
    this.expirationTime = expirationTime;
  }
}
