package sv.edu.udb.data_collector.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import sv.edu.udb.data_collector.domain.User;
import sv.edu.udb.data_collector.repository.UserRepository;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@RequiredArgsConstructor
public class JpaAuditingConfig {
    private final UserRepository userRepository;

    @Bean
    public AuditorAware<User> auditorAware() {
        return () -> {
            var ctx = SecurityContextHolder.getContext();
            if (ctx == null || ctx.getAuthentication() == null || !ctx.getAuthentication().isAuthenticated())
                return Optional.empty();
            String email = ctx.getAuthentication().getName();
            return userRepository.findByEmail(email);
        };
    }
}
