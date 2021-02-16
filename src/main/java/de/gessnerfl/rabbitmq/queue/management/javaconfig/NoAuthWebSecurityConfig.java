package de.gessnerfl.rabbitmq.queue.management.javaconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@ConditionalOnProperty(prefix = "de.gessnerfl.security.authentication", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoAuthWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.sessionManagement().disable();
        http.authorizeRequests().anyRequest().permitAll();
    }

}
