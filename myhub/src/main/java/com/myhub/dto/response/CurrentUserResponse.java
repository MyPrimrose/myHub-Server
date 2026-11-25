package com.myhub.dto.response;

import com.myhub.entity.UserAccount;

public record CurrentUserResponse(Long id, String username, String nickname) {
    public static CurrentUserResponse from(UserAccount user) { return new CurrentUserResponse(user.id(), user.username(), user.nickname()); }
}
