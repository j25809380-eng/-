package com.fitnote.backend.common;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id() {
        return optionalId().orElseThrow(() -> new IllegalStateException("未登录用户无法访问该接口"));
    }

    public static Optional<Long> optionalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Optional.empty();
        }

        String principal = authentication.getPrincipal().toString();
        if (principal.isBlank() || "anonymousUser".equals(principal)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(principal));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
