package com.softjourn.coin.auth.config;


import com.softjourn.coin.auth.entity.Token;
import com.softjourn.coin.auth.repository.TokenRepository;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Token store that is mix of JWT and JDBC token store.
 * It uses JWT tokens but store refresh tokens and so allow to revoke it
 */
public class RevocableJwtTokenStore implements TokenStore {

    private static final int DEFAULT_CLEAN_UP_INTERVAL = 100;

    private TokenRepository repository;

    private JwtTokenStore plainStore;

    private AtomicInteger counter = new AtomicInteger(0);

    /**
     * Create RevocableJwtTokenStore on plain JwtTokenStore and token repository to store refresh tokens
     * @param plainStore JwtTokenStore that will create tokens
     * @param repository repository to save refresh tokens
     */
    public RevocableJwtTokenStore(JwtTokenStore plainStore, TokenRepository repository) {
        this.plainStore = plainStore;
        this.repository = repository;
    }


    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return plainStore.readAuthentication(token);
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        return plainStore.readAuthentication(token);
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        plainStore.storeAccessToken(token, authentication);
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        return plainStore.readAccessToken(tokenValue);
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken token) {
        plainStore.removeAccessToken(token);
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        String tokenValue = refreshToken.getValue();
        //this expiration time is local so we need to add zone offset
        Instant expirationTime = ((DefaultExpiringOAuth2RefreshToken)refreshToken).getExpiration().toInstant();

        int offsetInSeconds = ZoneId.systemDefault().getRules().getOffset(Instant.now()).getTotalSeconds();
        expirationTime = expirationTime.plusSeconds(offsetInSeconds);
        Token token = new Token(tokenValue, expirationTime);
        repository.save(token);
        plainStore.storeRefreshToken(refreshToken, authentication);
        cleanUp();
    }

    private void cleanUp() {
        if(counter.incrementAndGet() > DEFAULT_CLEAN_UP_INTERVAL) {
            counter.set(0);
            repository.deleteExpired();
        }
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        DefaultExpiringOAuth2RefreshToken refreshToken = (DefaultExpiringOAuth2RefreshToken) plainStore.readRefreshToken(tokenValue);
        if (repository.isTokenAlive(refreshToken.getValue())) {
            return refreshToken;
        } else {
            return null;
        }
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return plainStore.readAuthenticationForRefreshToken(token);
    }

    @Override
    @Transactional
    public void removeRefreshToken(OAuth2RefreshToken token) {
        repository.delete(token.getValue());
        plainStore.removeRefreshToken(token);
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        plainStore.removeAccessTokenUsingRefreshToken(refreshToken);
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        return plainStore.getAccessToken(authentication);
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        return plainStore.findTokensByClientIdAndUserName(clientId, userName);
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        return plainStore.findTokensByClientId(clientId);
    }

}
