package de.gessnerfl.rabbitmq.queue.management.javaconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(prefix = "de.gessnerfl.security.authentication", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoAuthWebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable())
                .sessionManagement(s -> s.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

}
