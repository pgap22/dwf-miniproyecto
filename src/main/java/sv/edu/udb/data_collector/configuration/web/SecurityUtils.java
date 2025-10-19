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
}
