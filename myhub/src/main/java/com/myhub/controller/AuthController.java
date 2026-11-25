package com.myhub.controller;

import com.myhub.common.result.Result;
import com.myhub.dto.request.LoginRequest;
import com.myhub.dto.response.CurrentUserResponse;
import com.myhub.entity.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager manager; private final JdbcTemplate jdbc;
    public AuthController(AuthenticationManager manager, JdbcTemplate jdbc) { this.manager=manager; this.jdbc=jdbc; }
    @GetMapping("/csrf") public Result<Map<String,String>> csrf(CsrfToken token) { return Result.success(Map.of("token", token.getToken(), "headerName", token.getHeaderName())); }
    @PostMapping("/login") public Result<CurrentUserResponse> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        Authentication auth=manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));
        SecurityContext context=SecurityContextHolder.createEmptyContext(); context.setAuthentication(auth); SecurityContextHolder.setContext(context);
        // Create the pre-authentication session first, then rotate its ID before
        // associating the authenticated SecurityContext with it.
        var session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        UserAccount user=(UserAccount)auth.getPrincipal(); jdbc.update("UPDATE sys_user SET last_login_time=? WHERE id=?", LocalDateTime.now(), user.id());
        return new Result<>(200,"登录成功",CurrentUserResponse.from(user),System.currentTimeMillis());
    }
    @GetMapping("/me") public Result<CurrentUserResponse> me(Authentication auth) { return Result.success(CurrentUserResponse.from((UserAccount)auth.getPrincipal())); }
}
