package com.softjourn.coin.auth.service;

import com.softjourn.coin.auth.entity.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Profile("jenkins")
public class MockLdapService implements ILdapService {
    @Override
    public List<User> getAllUsers() {
        return Collections.emptyList();
    }

    @Override
    public boolean userExist(User user) {
        return true;
    }

    @Override
    public User getUser(String ldapId) {
        return new User("test", "Test Test", "test@Test.com", Collections.emptySet());
    }
}
