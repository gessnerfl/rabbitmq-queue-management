package de.gessnerfl.rabbitmq.queue.management.javaconfig;

import de.gessnerfl.rabbitmq.queue.management.service.security.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.ldap.EmbeddedLdapServerContextSourceFactoryBean;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@ConditionalOnProperty(prefix = "de.gessnerfl.security.authentication", name = "enabled", havingValue = "true")
public class LdapAuthWebSecurityConfig {
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
                .anonymous(a -> a.principal(ANONYMOUS_USER))
                .requestCache(rc -> rc.disable())
                .securityContext(sc -> sc.securityContextRepository(cookieSecurityContextRepository()))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(loginWithTargetUrlAuthenticationEntryPoint()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a ->
                        a.requestMatchers("/css/**").permitAll()
                                .requestMatchers("/gfx/**").permitAll()
                                .requestMatchers("/webjars/**").permitAll()
                                .requestMatchers("/actuator/**").permitAll()
                                .anyRequest().fullyAuthenticated())
                .formLogin(fl ->
                        fl.loginPage(LOGIN_FORM_URL)
                                .successHandler(redirectToOriginalUrlAuthenticationSuccessHandler())
                                .failureUrl("/login-error")
                                .permitAll())
                .logout(lo ->
                        lo.invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .logoutSuccessUrl("/login?logout")
                                .permitAll()
                                .deleteCookies(JWT_TOKEN_COOKIE_NAME));

        http.exceptionHandling(eh -> eh.accessDeniedPage(LOGIN_FORM_URL));
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "de.gessnerfl.security.authentication.ldap", name = "url", matchIfMissing = false)
    public ContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapAuthenticationConfig.getContextSource().getUrl());
        contextSource.setUserDn(ldapAuthenticationConfig.getContextSource().getManagerDn());
        contextSource.setPassword(ldapAuthenticationConfig.getContextSource().getManagerPassword());
        contextSource.setPooled(true);
        contextSource.setBase(ldapAuthenticationConfig.getContextSource().getRoot());
        return contextSource;
    }

    @Bean
    @ConditionalOnProperty(prefix = "de.gessnerfl.security.authentication.ldap", name = "url", matchIfMissing = true)
    public EmbeddedLdapServerContextSourceFactoryBean embeddedLdapServerContextSourceFactoryBean() {
        EmbeddedLdapServerContextSourceFactoryBean bean = EmbeddedLdapServerContextSourceFactoryBean.fromEmbeddedLdapServer();
        bean.setPort(ldapAuthenticationConfig.getContextSource().getPort());
        bean.setLdif(ldapAuthenticationConfig.getContextSource().getLdif());
        bean.setManagerDn(ldapAuthenticationConfig.getContextSource().getManagerDn());
        bean.setManagerPassword(ldapAuthenticationConfig.getContextSource().getManagerPassword());
        bean.setRoot(ldapAuthenticationConfig.getContextSource().getRoot());
        return bean;
    }

    @Bean
    LdapAuthoritiesPopulator authorities(BaseLdapPathContextSource contextSource) {
        String groupSearchBase = ldapAuthenticationConfig.getGroupSearchBase();
        DefaultLdapAuthoritiesPopulator authorities = new DefaultLdapAuthoritiesPopulator(contextSource, groupSearchBase);
        authorities.setGroupSearchFilter(ldapAuthenticationConfig.getGroupSearchFilter());
        authorities.setRolePrefix("ROLE_");
        authorities.setGroupRoleAttribute(ldapAuthenticationConfig.getGroupRoleAttribute());
        return authorities;
    }

    @Bean
    AuthenticationManager authenticationManager(BaseLdapPathContextSource contextSource, LdapAuthoritiesPopulator authorities) {
        LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserDnPatterns(ldapAuthenticationConfig.getUserDnPatterns().toArray(new String[ldapAuthenticationConfig.getUserDnPatterns().size()]));
        factory.setUserSearchFilter(ldapAuthenticationConfig.getUserSearchFilter());
        factory.setUserSearchBase(ldapAuthenticationConfig.getUserSearchBase());
        factory.setLdapAuthoritiesPopulator(authorities);
        return factory.createAuthenticationManager();
    }

    @Bean
    public CookieSecurityContextRepository cookieSecurityContextRepository() {
        var logger = LoggerFactory.getLogger(CookieSecurityContextRepository.class);
        return new CookieSecurityContextRepository(jwtTokenProvider(), logger);
    }

    @Bean
    public JWTTokenProvider jwtTokenProvider() {
        return new JWTTokenProvider(jwtConfig);
    }

    @Bean
    public RedirectToOriginalUrlAuthenticationSuccessHandler redirectToOriginalUrlAuthenticationSuccessHandler() {
        return new RedirectToOriginalUrlAuthenticationSuccessHandler();
    }

    @Bean
    public LoginWithTargetUrlAuthenticationEntryPoint loginWithTargetUrlAuthenticationEntryPoint() {
        return new LoginWithTargetUrlAuthenticationEntryPoint();
    }
}
