package com.myhub;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database-independent tests for the password format used by login.
 * BCrypt is one-way: never decrypt or compare encoded strings directly; use matches().
 * The stored sadmin hash test also guards against accidentally using "admin" as its password.
 */
class PasswordEncoderTests {
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void encodedPasswordMatchesOriginalAndNotWrongPassword() {
        String rawPassword = "sadmin";
        String hash = encoder.encode(rawPassword);

        System.out.println("Encoded password: " + hash);
        assertNotEquals(rawPassword, hash, "The database value must never be plaintext");
        assertTrue(hash.startsWith("$2a$10$"), "Expected the configured BCrypt format and strength");
        assertTrue(encoder.matches(rawPassword, hash));
        assertFalse(encoder.matches("admin", hash));
    }

    @Test
    void storedSadminHashMatchesSadminButNotAdmin() {
        String storedHash = "$2a$10$QJMwx24yNtkWqgwT8HOIgecNuT7FYII39k2vmeEYQuwEXmnjb312y";

        assertTrue(encoder.matches("sadmin", storedHash));
        assertFalse(encoder.matches("admin", storedHash));
    }
}
