package com.myhub.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;

@Configuration
public class AuthServiceConfig {
    @Bean UserDetailsService userDetailsService(JdbcTemplate jdbc) {
        return username -> jdbc.query("SELECT id,username,password,nickname,avatar,status FROM sys_user WHERE username=?", rs -> {
            if (!rs.next()) throw new UsernameNotFoundException("Invalid credentials");
            return new UserAccount(rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getString("nickname"), rs.getString("avatar"), rs.getInt("status")==1);
        }, username);
    }
    @Bean DaoAuthenticationProvider authenticationProvider(UserDetailsService users, PasswordEncoder encoder) {
        var provider=new DaoAuthenticationProvider(users); provider.setPasswordEncoder(encoder); return provider;
    }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { return config.getAuthenticationManager(); }
    @Bean CommandLineRunner initialAdmin(JdbcTemplate jdbc, PasswordEncoder encoder) {
        return args -> {
            Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class);
            if (count != null && count > 0) return;
            String username=System.getenv("MYHUB_ADMIN_USERNAME"), password=System.getenv("MYHUB_ADMIN_PASSWORD");
            if (username==null || username.isBlank() || password==null || password.length()<12)
                throw new IllegalStateException("No admin account exists. Set MYHUB_ADMIN_USERNAME and MYHUB_ADMIN_PASSWORD (at least 12 characters) before starting.");
            jdbc.update("INSERT INTO sys_user(username,password,nickname,status) VALUES(?,?,?,1)", username, encoder.encode(password), "MyHub");
        };
    }
}
