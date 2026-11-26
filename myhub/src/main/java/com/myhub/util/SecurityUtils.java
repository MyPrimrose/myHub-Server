package com.myhub.util;

import com.myhub.common.exception.BusinessException;
import com.myhub.entity.UserAccount;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static UserAccount requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserAccount user)) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }

    public static long requireCurrentUserId() {
        return requireCurrentUser().id();
    }
}
