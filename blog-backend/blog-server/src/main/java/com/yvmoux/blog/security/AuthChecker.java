package com.yvmoux.blog.security;

import com.yvmoux.blog.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authChecker")
@RequiredArgsConstructor
public class AuthChecker {
    public boolean isActive() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (auth.getPrincipal() instanceof AppUserDetails userDetails) {
            return UserStatus.ACTIVE.name().equals(userDetails.getStatus());
        }
        return false;
    }
}
