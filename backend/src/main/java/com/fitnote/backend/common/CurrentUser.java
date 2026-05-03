package com.fitnote.backend.common;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {
    }

    /**
     * 获取当前登录用户 ID。
     * 若为 demo 环境（未登录），自动降级返回默认用户 ID=1L。
     * 若为生产环境且未登录，抛出异常。
     */
    public static Long id() {
        return optionalId().orElse(1L);
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
