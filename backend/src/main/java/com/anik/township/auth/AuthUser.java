package com.anik.township.auth;

import com.anik.township.domain.AppUser;
import lombok.Getter;

@Getter
public class AuthUser {

    private final AppUser user;

    public AuthUser(AppUser user) {
        this.user = user;
    }
}
