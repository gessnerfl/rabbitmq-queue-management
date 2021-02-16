package de.gessnerfl.rabbitmq.queue.management.service.security;

import de.gessnerfl.rabbitmq.queue.management.javaconfig.LdapAuthWebSecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.stream.Stream;

public class CookieSecurityContextRepository implements SecurityContextRepository {

    private static final String EMPTY_CREDENTIALS = "";

    private final JWTTokenProvider jwtTokenProvider;
    private final Logger logger;

    @Autowired
    public CookieSecurityContextRepository(JWTTokenProvider jwtTokenProvider, Logger logger) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.logger = logger;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        var request = requestResponseHolder.getRequest();
        var response = requestResponseHolder.getResponse();
        requestResponseHolder.setResponse(new SaveToCookieResponseWrapper(request, response, jwtTokenProvider, LoggerFactory.getLogger(SaveToCookieResponseWrapper.class)));

        var context = SecurityContextHolder.createEmptyContext();
        readUserInfoFromCookie(request).ifPresent(userDetails ->
                context.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, EMPTY_CREDENTIALS, userDetails.getAuthorities())));

        return context;
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        SaveToCookieResponseWrapper responseWrapper = (SaveToCookieResponseWrapper) response;
        if (!responseWrapper.isContextSaved()) {
            responseWrapper.saveContext(context);
        }
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return readUserInfoFromCookie(request).isPresent();
    }

    private Optional<UserDetails> readUserInfoFromCookie(HttpServletRequest request) {
        return readCookieFromRequest(request).map(Cookie::getValue).map(jwtTokenProvider::getUserDetailsFromToken);
    }

    private Optional<Cookie> readCookieFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            logger.debug("No cookies in request");
            return Optional.empty();
        }

        var maybeCookie = Stream.of(request.getCookies())
                .filter(c -> LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME.equals(c.getName()))
                .findFirst();

        if(maybeCookie.isEmpty()){
            logger.debug("No {} cookie in request", LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME);
        }
        return maybeCookie;
    }

    static class SaveToCookieResponseWrapper extends SaveContextOnUpdateOrErrorResponseWrapper {
        private final Logger logger;
        private final HttpServletRequest request;
        private final JWTTokenProvider jwtTokenProvider;

        SaveToCookieResponseWrapper(HttpServletRequest request, HttpServletResponse response, JWTTokenProvider jwtTokenProvider, Logger logger) {
            super(response, true);
            this.request = request;
            this.jwtTokenProvider = jwtTokenProvider;
            this.logger = logger;
        }

        @Override
        protected void saveContext(SecurityContext securityContext) {
            HttpServletResponse response = (HttpServletResponse) getResponse();
            Authentication authentication = securityContext.getAuthentication();
            if (authentication == null) {
                logger.debug("No user authenticated, skip saveContext");
                return;
            }

            if (LdapAuthWebSecurityConfig.ANONYMOUS_USER.equals(authentication.getPrincipal())) {
                logger.debug("Anonymous User logged in, skip saveContext");
                return;
            }

            if (!(authentication.getPrincipal() instanceof UserDetails)) {
                logger.warn("Principal of unsupported type {}, skip saveContext", authentication.getPrincipal().getClass().getCanonicalName());
                return;
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            var jwtToken = jwtTokenProvider.createToken(userDetails);
            var cookie = new Cookie(LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME, jwtToken);
            cookie.setSecure(request.isSecure());
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            logger.debug("SecurityContext for principal '{}' saved in Cookie", userDetails.getUsername());
        }
    }
}
