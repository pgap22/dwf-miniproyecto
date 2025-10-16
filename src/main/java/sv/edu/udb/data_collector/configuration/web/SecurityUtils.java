package sv.edu.udb.data_collector.configuration.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils(){}

    public static String currentEmailOrNull() {
        var ctx = SecurityContextHolder.getContext();
        Authentication a = (ctx == null) ? null : ctx.getAuthentication();
        return (a == null || !a.isAuthenticated()) ? null : a.getName();
    }

    public static boolean isAdmin() {
        var ctx = SecurityContextHolder.getContext();
        Authentication a = (ctx == null) ? null : ctx.getAuthentication();
        if (a == null || a.getAuthorities() == null) return false;
        return a.getAuthorities().stream().anyMatch(ga -> "ROLE_ADMIN".equals(ga.getAuthority()));
    }
}
