package com.myhub;

import com.myhub.entity.UserAccount;
import com.myhub.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/** Checks the credentials returned by MyBatis from the database configured for this app. */
@SpringBootTest
class UserPasswordDatabaseTests {
    @Autowired UserMapper userMapper;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void databaseSadminRowHasExpectedPasswordAndEnabledStatus() {
        UserAccount user = userMapper.selectByUsername("sadmin");
        assertNotNull(user, "The connected database must contain username sadmin");
        assertTrue(user.isEnabled(), "sadmin must have status=1");
        assertTrue(passwordEncoder.matches("sadmin", user.getPassword()),
                "The password hash loaded by MyBatis must match raw password sadmin");
    }
}
