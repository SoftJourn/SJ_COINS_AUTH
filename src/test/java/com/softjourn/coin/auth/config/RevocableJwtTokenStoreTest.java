package com.softjourn.coin.auth.config;

import com.softjourn.coin.auth.entity.Token;
import com.softjourn.coin.auth.repository.TokenRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class RevocableJwtTokenStoreTest {

    private static final String TOKEN_VALUE = "token";
    private static final String NEW_TOKEN_VALUE = "newtoken";

    Instant expiration = Instant.ofEpochSecond(1_000_000);
    DefaultExpiringOAuth2RefreshToken newToken = new DefaultExpiringOAuth2RefreshToken(NEW_TOKEN_VALUE, new Date(expiration.toEpochMilli()));

    @Mock
    TokenRepository repository;

    @Mock
    JwtTokenStore jwtTokenStore;

    @Mock
    Clock clock;

    @Mock
    DefaultExpiringOAuth2RefreshToken token;

    @InjectMocks
    RevocableJwtTokenStore tokenStore;

    OAuth2Authentication auth2Authentication;

    Map<String, Instant> tokensMap = new HashMap<String, Instant>() {{
        put(TOKEN_VALUE, Instant.ofEpochSecond(1_000_000));
    }};

    @Before
    public void setUp() throws Exception {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(500_000));

        when(repository.getAliveTokens()).thenReturn(new ArrayList<>(tokensMap.keySet()));
        when(repository.isTokenAlive(anyString())).then(i -> {
            Instant instant = tokensMap.get(i.getArguments()[0]);
            return instant != null && instant.isAfter(Instant.now(clock));
        });

        doAnswer(i -> {
            String t = (String) i.getArguments()[0];
            tokensMap.remove(t);
            return null;
        }).when(repository).delete(anyString());

        doAnswer(i -> {
            Token token = (Token) i.getArguments()[0];
            tokensMap.put(token.getTokenValue(), token.getExpirationTime());
            return token;
        }).when(repository).save(any(Token.class));

        when(jwtTokenStore.readRefreshToken(TOKEN_VALUE)).thenReturn(token);
        when(jwtTokenStore.readRefreshToken(NEW_TOKEN_VALUE)).thenReturn(newToken);

        when(token.getValue()).thenReturn(TOKEN_VALUE);
        when(token.getExpiration()).thenReturn(Date.from(Instant.ofEpochSecond(1_000_000)));

        auth2Authentication = mock(OAuth2Authentication.class);

    }

    @Test
    public void storeTokenTest() {

        assertNull(tokenStore.readRefreshToken(NEW_TOKEN_VALUE));

        tokenStore.storeRefreshToken(newToken, auth2Authentication);

        DefaultExpiringOAuth2RefreshToken token = (DefaultExpiringOAuth2RefreshToken) tokenStore.readRefreshToken(NEW_TOKEN_VALUE);

        assertEquals(NEW_TOKEN_VALUE, token.getValue());
        assertEquals(expiration, token.getExpiration().toInstant());

    }

    @Test
    public void readRefreshToken() {
        assertEquals(token, tokenStore.readRefreshToken(TOKEN_VALUE));

        when(clock.instant()).thenReturn(Instant.ofEpochSecond(1_500_000));

        assertNull(tokenStore.readRefreshToken(TOKEN_VALUE));
    }

    @Test
    public void testRemove() {
        assertEquals(token, tokenStore.readRefreshToken(TOKEN_VALUE));

        tokenStore.removeRefreshToken(token);

        assertNull(tokenStore.readRefreshToken(TOKEN_VALUE));
    }

    @Test
    public void testCleanUp() {
        for (int i = 0; i < 205; i++) {
            tokenStore.storeRefreshToken(token, auth2Authentication);
        }

        verify(repository, atLeast(2)).deleteExpired();
    }




}