package com.myhub.security;

public record CurrentUserVO(Long id, String username, String nickname) {
    public static CurrentUserVO from(UserAccount user) { return new CurrentUserVO(user.id(), user.username(), user.nickname()); }
}
