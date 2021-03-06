package de.gessnerfl.rabbitmq.queue.management.javaconfig;

import de.gessnerfl.rabbitmq.queue.management.service.security.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@ConditionalOnProperty(prefix = "de.gessnerfl.security.authentication", name = "enabled", havingValue = "true")
public class LdapAuthWebSecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String ANONYMOUS_USER = "anonymousUser";
    public static final String JWT_TOKEN_COOKIE_NAME = "rmqqm-token";
    public static final String TARGET_AFTER_SUCCESSFUL_LOGIN_PARAM = "target-url";
    public static final String LOGIN_FORM_URL = "/login";

    private final LdapAuthenticationConfig ldapAuthenticationConfig;
    private final JWTConfig jwtConfig;

    @Autowired
    public LdapAuthWebSecurityConfig(LdapAuthenticationConfig ldapAuthenticationConfig, JWTConfig jwtConfig) {
        this.ldapAuthenticationConfig = ldapAuthenticationConfig;
        this.jwtConfig = jwtConfig;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .anonymous().principal(ANONYMOUS_USER).and()
            .requestCache().disable()
            .securityContext().securityContextRepository(cookieSecurityContextRepository()).and()
            .exceptionHandling().authenticationEntryPoint(loginWithTargetUrlAuthenticationEntryPoint()).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
                .antMatchers("/css/**").permitAll()
                .antMatchers("/gfx/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .anyRequest().fullyAuthenticated()
                .and()
                .formLogin()
                .loginPage(LOGIN_FORM_URL)
                .successHandler(redirectToOriginalUrlAuthenticationSuccessHandler())
                .failureUrl("/login-error")
                .permitAll()
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                .deleteCookies(JWT_TOKEN_COOKIE_NAME);

        http.exceptionHandling().accessDeniedPage(LOGIN_FORM_URL);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.ldapAuthentication()
                .groupRoleAttribute(ldapAuthenticationConfig.getGroupRoleAttribute())
                .groupSearchBase(ldapAuthenticationConfig.getGroupSearchBase())
                .groupSearchFilter(ldapAuthenticationConfig.getGroupSearchFilter())
                .rolePrefix("ROLE_")
                .userSearchBase(ldapAuthenticationConfig.getUserSearchBase())
                .userSearchFilter(ldapAuthenticationConfig.getUserSearchFilter())
                .userDnPatterns(ldapAuthenticationConfig.getUserDnPatterns().toArray(new String[ldapAuthenticationConfig.getUserDnPatterns().size()]))
                .contextSource()
                    .managerDn(ldapAuthenticationConfig.getContextSource().getManagerDn())
                    .managerPassword(ldapAuthenticationConfig.getContextSource().getManagerPassword())
                    .root(ldapAuthenticationConfig.getContextSource().getRoot())
                    .port(ldapAuthenticationConfig.getContextSource().getPort())
                    .url(ldapAuthenticationConfig.getContextSource().getUrl())
                    .ldif(ldapAuthenticationConfig.getContextSource().getLdif());
    }

    @Bean
    public CookieSecurityContextRepository cookieSecurityContextRepository(){
        var logger = LoggerFactory.getLogger(CookieSecurityContextRepository.class);
        return new CookieSecurityContextRepository(jwtTokenProvider(), logger);
    }

    @Bean
    public JWTTokenProvider jwtTokenProvider(){
        return new JWTTokenProvider(jwtConfig);
    }

    @Bean
    public RedirectToOriginalUrlAuthenticationSuccessHandler redirectToOriginalUrlAuthenticationSuccessHandler(){
        return new RedirectToOriginalUrlAuthenticationSuccessHandler();
    }

    @Bean
    public LoginWithTargetUrlAuthenticationEntryPoint loginWithTargetUrlAuthenticationEntryPoint(){
        return new LoginWithTargetUrlAuthenticationEntryPoint();
    }
}
