package de.gessnerfl.rabbitmq.queue.management.service.security;

import de.gessnerfl.rabbitmq.queue.management.javaconfig.LdapAuthWebSecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectToOriginalUrlAuthenticationSuccessHandler  extends SimpleUrlAuthenticationSuccessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectToOriginalUrlAuthenticationSuccessHandler.class);
    private static final String DEFAULT_TARGET_URL = "/";

    public RedirectToOriginalUrlAuthenticationSuccessHandler() {
        super(DEFAULT_TARGET_URL);
        this.setTargetUrlParameter(LdapAuthWebSecurityConfig.TARGET_AFTER_SUCCESSFUL_LOGIN_PARAM);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        var targetUrl = super.determineTargetUrl(request, response, authentication);
        if (UrlUtils.isAbsoluteUrl(targetUrl)) {
            LOGGER.warn("Absolute target URL {} identified and suppressed", targetUrl.replaceAll("[\n\r\t]", "_"));
            return DEFAULT_TARGET_URL;
        }
        return targetUrl;
    }
}
