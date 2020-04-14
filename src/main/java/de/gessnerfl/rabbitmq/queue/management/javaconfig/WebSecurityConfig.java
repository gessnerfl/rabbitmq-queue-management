package de.gessnerfl.rabbitmq.queue.management.javaconfig;

import de.gessnerfl.rabbitmq.queue.management.service.security.AuthenticationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
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
            http.authorizeRequests()
                    .antMatchers("/css/**").permitAll()
                    .antMatchers("/gfx/**").permitAll()
                    .antMatchers("/webjars/**").permitAll()
                    .antMatchers("/actuator/**").permitAll()
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
            http.authorizeRequests()
                    .anyRequest().permitAll();
        }
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        if (authenticationConfig.isEnabled()) {
            AuthenticationConfig.LdapAuthenticationConfiguration ldap = authenticationConfig.getLdap();
            auth.ldapAuthentication()
                    .groupRoleAttribute(ldap.getGroupRoleAttribute())
                    .groupSearchBase(ldap.getGroupSearchBase())
                    .groupSearchFilter(ldap.getGroupSearchFilter())
                    .rolePrefix("ROLE_")
                    .userSearchBase(ldap.getUserSearchBase())
                    .userSearchFilter(ldap.getUserSearchFilter())
                    .userDnPatterns(ldap.getUserDnPatterns().toArray(new String[ldap.getUserDnPatterns().size()]))
                    .contextSource()
                        .managerDn(ldap.getContextSource().getManagerDn())
                        .managerPassword(ldap.getContextSource().getManagerPassword())
                        .root(ldap.getContextSource().getRoot())
                        .port(ldap.getContextSource().getPort())
                        .url(ldap.getContextSource().getUrl())
                        .ldif(ldap.getContextSource().getLdif());
        }
    }

}
