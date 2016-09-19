package com.softjourn.coin.auth.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

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
