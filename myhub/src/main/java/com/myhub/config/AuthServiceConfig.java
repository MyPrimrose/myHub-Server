package com.myhub.config;

import com.myhub.entity.UserAccount;
import com.myhub.mapper.UserMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;

@Configuration
public class AuthServiceConfig {
    @Bean UserDetailsService userDetailsService(UserMapper userMapper) {
        return username -> {
            UserAccount user = userMapper.selectByUsername(username);
            if (user == null) throw new UsernameNotFoundException("Invalid credentials");
            return user;
        };
    }
    @Bean DaoAuthenticationProvider authenticationProvider(UserDetailsService users, PasswordEncoder encoder) {
        var provider=new DaoAuthenticationProvider(users); provider.setPasswordEncoder(encoder); return provider;
    }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { return config.getAuthenticationManager(); }
    @Bean CommandLineRunner initialAdmin(UserMapper userMapper, PasswordEncoder encoder) {
        return args -> {
            if (userMapper.countUsers() > 0) return;
            String username=System.getenv("MYHUB_ADMIN_USERNAME"), password=System.getenv("MYHUB_ADMIN_PASSWORD");
            if (username==null || username.isBlank() || password==null || password.length()<12)
                throw new IllegalStateException("No admin account exists. Set MYHUB_ADMIN_USERNAME and MYHUB_ADMIN_PASSWORD (at least 12 characters) before starting.");
            userMapper.insertInitialUser(username, encoder.encode(password), "MyHub");
        };
    }
}
