package de.gessnerfl.rabbitmq.queue.management.service.security;

import de.gessnerfl.rabbitmq.queue.management.javaconfig.LdapAuthWebSecurityConfig;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginWithTargetUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    public LoginWithTargetUrlAuthenticationEntryPoint() {
        super(LdapAuthWebSecurityConfig.LOGIN_FORM_URL);
    }

    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        return UriComponentsBuilder.fromUriString(super.determineUrlToUseForThisRequest(request, response, exception))
                .queryParam(LdapAuthWebSecurityConfig.TARGET_AFTER_SUCCESSFUL_LOGIN_PARAM, request.getRequestURI())
                .toUriString();
    }
}
