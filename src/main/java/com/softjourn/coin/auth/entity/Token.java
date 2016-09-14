package com.softjourn.coin.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class Token {

    @Id
    @Column(name = "value", length = 1024)
    private String tokenValue;

    @Column(name = "expiration")
    private Instant expirationTime;

}
