package com.myhub.security;

import com.myhub.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    public record LoginRequest(@NotBlank @Size(max=64) String username, @NotBlank @Size(max=128) String password) {}
    @GetMapping("/csrf") public Result<Map<String,String>> csrf(CsrfToken token) { return Result.success(Map.of("token", token.getToken(), "headerName", token.getHeaderName())); }
    @PostMapping("/login") public Result<CurrentUserVO> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        Authentication auth=manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));
        SecurityContext context=SecurityContextHolder.createEmptyContext(); context.setAuthentication(auth); SecurityContextHolder.setContext(context);
        // Create the pre-authentication session first, then rotate its ID before
        // associating the authenticated SecurityContext with it.
        var session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        UserAccount user=(UserAccount)auth.getPrincipal(); jdbc.update("UPDATE sys_user SET last_login_time=? WHERE id=?", LocalDateTime.now(), user.id());
        return new Result<>(200,"登录成功",CurrentUserVO.from(user),System.currentTimeMillis());
    }
    @GetMapping("/me") public Result<CurrentUserVO> me(Authentication auth) { return Result.success(CurrentUserVO.from((UserAccount)auth.getPrincipal())); }
}
