package de.gessnerfl.rabbitmq.queue.management.service.security;

import de.gessnerfl.rabbitmq.queue.management.javaconfig.LdapAuthWebSecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;

public class RedirectToOriginalUrlAuthenticationSuccessHandler  extends SimpleUrlAuthenticationSuccessHandler {
    private static final String DEFAULT_TARGET_URL = "/";

    public RedirectToOriginalUrlAuthenticationSuccessHandler() {
        super(DEFAULT_TARGET_URL);
        this.setTargetUrlParameter(LdapAuthWebSecurityConfig.TARGET_AFTER_SUCCESSFUL_LOGIN_PARAM);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        var targetUrl = super.determineTargetUrl(request, response, authentication);
        if (UrlUtils.isAbsoluteUrl(targetUrl)) {
            return DEFAULT_TARGET_URL;
        }
        return targetUrl;
    }
}
