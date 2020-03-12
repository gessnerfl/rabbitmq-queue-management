package de.gessnerfl.rabbitmq.queue.management.javaconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationConfig authenticationConfig;

    @Autowired
    public WebSecurityConfig(AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (authenticationConfig.isEnabled()) {
            http
                    .authorizeRequests()
                    .antMatchers("/css/**").permitAll()
                    .antMatchers("/gfx/**").permitAll()
                    .antMatchers("/webjars/**").permitAll()
                    .anyRequest().fullyAuthenticated()
                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .failureUrl("/login-error")
                    .permitAll()
                    .and()
                    .logout()
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/login?logout")
                    .permitAll();
        } else {
            http.authorizeRequests().anyRequest().permitAll();
        }
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        if (authenticationConfig.isEnabled()) {
            PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            auth.inMemoryAuthentication().withUser("rabbit").password(encoder.encode("rabbit")).roles("ADMIN");
//            auth
//                    .ldapAuthentication()
//                    .userDnPatterns("uid={0},ou=people")
//                    .groupSearchBase("ou=groups")
//                    .contextSource()
//                    .url("ldap://localhost:8389/dc=springframework,dc=org")
//                    .and()
//                    .passwordCompare()
//                    .passwordEncoder(new BCryptPasswordEncoder())
//                    .passwordAttribute("userPassword");
        }
    }

}
