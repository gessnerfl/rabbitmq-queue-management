package de.gessnerfl.rabbitmq.queue.management.service.security;

import de.gessnerfl.rabbitmq.queue.management.javaconfig.LdapAuthWebSecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpRequestResponseHolder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookieSecurityContextRepositoryTest {

    @Mock
    private JWTTokenProvider jwtTokenProvider;
    @Mock
    private Logger logger;

    @InjectMocks
    private CookieSecurityContextRepository sut;

    @Test
    void shouldSetAuthenticationInContextAndSetResponseWrapperWhenLoadContextIsRequestedAndCookieIsAvailable(){
        var userDetails = mock(UserDetails.class);
        Collection roles = Arrays.asList(new SimpleGrantedAuthority("role1"), new SimpleGrantedAuthority("role2"));
        var token = "token";
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var requestResponseHolder = new HttpRequestResponseHolder(request, response);
        var cookie = mock(Cookie.class);

        when(cookie.getName()).thenReturn(LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME);
        when(cookie.getValue()).thenReturn(token);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtTokenProvider.getUserDetailsFromToken(token)).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(roles);

        var result = sut.loadContext(requestResponseHolder);

        assertEquals(userDetails, result.getAuthentication().getPrincipal());
        assertEquals("", result.getAuthentication().getCredentials());
        assertEquals(roles, result.getAuthentication().getAuthorities());
        assertThat(requestResponseHolder.getResponse(), instanceOf(CookieSecurityContextRepository.SaveToCookieResponseWrapper.class));
        verify(jwtTokenProvider).getUserDetailsFromToken(token);
        verifyNoMoreInteractions(jwtTokenProvider);
    }

    @Test
    void shouldNotSetAuthenticationInContextAndSetResponseWrapperWhenLoadContextIsRequestedAndNoCookiesAreAvailable(){
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var requestResponseHolder = new HttpRequestResponseHolder(request, response);

        when(request.getCookies()).thenReturn(null);

        var result = sut.loadContext(requestResponseHolder);

        assertNull(result.getAuthentication());
        assertThat(requestResponseHolder.getResponse(), instanceOf(CookieSecurityContextRepository.SaveToCookieResponseWrapper.class));
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void shouldNotSetAuthenticationInContextAndSetResponseWrapperWhenLoadContextIsRequestedAndCookieIsNotAvailable(){
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var requestResponseHolder = new HttpRequestResponseHolder(request, response);
        var cookie = mock(Cookie.class);

        when(cookie.getName()).thenReturn("other-cookie");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        var result = sut.loadContext(requestResponseHolder);

        assertNull(result.getAuthentication());
        assertThat(requestResponseHolder.getResponse(), instanceOf(CookieSecurityContextRepository.SaveToCookieResponseWrapper.class));
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void shouldThrowExceptionWhenWhenLoadContextIsRequestedAndJWTTokenIsNotValid(){
        var token = "invalid-token";
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var requestResponseHolder = new HttpRequestResponseHolder(request, response);
        var cookie = mock(Cookie.class);
        var expectedException = new InvalidJWTSigningKeyException();

        when(cookie.getName()).thenReturn(LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME);
        when(cookie.getValue()).thenReturn(token);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtTokenProvider.getUserDetailsFromToken(token)).thenThrow(expectedException);

        try {
            sut.loadContext(requestResponseHolder);
            fail();
        } catch (Exception e){
            assertSame(expectedException, e);
        }
        assertThat(requestResponseHolder.getResponse(), instanceOf(CookieSecurityContextRepository.SaveToCookieResponseWrapper.class));
    }

    @Test
    void shouldReturnTrueWhenAvailabilityInContextIsRequestedAndCookieIsAvailable(){
        var userDetails = mock(UserDetails.class);
        var token = "token";
        var request = mock(HttpServletRequest.class);
        var cookie = mock(Cookie.class);

        when(cookie.getName()).thenReturn(LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME);
        when(cookie.getValue()).thenReturn(token);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtTokenProvider.getUserDetailsFromToken(token)).thenReturn(userDetails);

        assertTrue(sut.containsContext(request));
    }

    @Test
    void shouldReturnFalseWhenAvailabilityInContextIsRequestedAndNoCookiesAreAvailable(){
        var request = mock(HttpServletRequest.class);

        when(request.getCookies()).thenReturn(null);

        assertFalse(sut.containsContext(request));
    }

    @Test
    void shouldReturnFalseWhenAvailabilityInContextIsRequestedAndCookieIsNotAvailable(){
        var request = mock(HttpServletRequest.class);
        var cookie = mock(Cookie.class);

        when(cookie.getName()).thenReturn("other-cookie");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        assertFalse(sut.containsContext(request));
    }

    @Test
    void shouldThrowExceptionWhenAvailabilityInContextIsRequestedAndJWTTokenIsNotValid(){
        var token = "ivalid-token";
        var request = mock(HttpServletRequest.class);
        var cookie = mock(Cookie.class);
        var expectedException = new InvalidJWTSigningKeyException();

        when(cookie.getName()).thenReturn(LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME);
        when(cookie.getValue()).thenReturn(token);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtTokenProvider.getUserDetailsFromToken(token)).thenThrow(expectedException);

        try {
            sut.containsContext(request);
            fail();
        } catch (Exception e){
            assertSame(expectedException, e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/foo"})
    void shouldSetCookieWhenSaveContextIsRequestedAndUserIsAuthenticatedAndTokenIsCreatedSuccessfully(String contextPath){
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var wrappedResponse = new CookieSecurityContextRepository.SaveToCookieResponseWrapper(request, response, jwtTokenProvider, logger);
        var context = mock(SecurityContext.class);
        var token = "token";
        var authentication = mock(Authentication.class);
        var userDetails = mock(UserDetails.class);

        when(request.isSecure()).thenReturn(true);
        when(request.getContextPath()).thenReturn(contextPath);
        when(context.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtTokenProvider.createToken(userDetails)).thenReturn(token);

        sut.saveContext(context, request, wrappedResponse);

        verify(jwtTokenProvider).createToken(userDetails);

        ArgumentCaptor<Cookie> cookieArgumentCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieArgumentCaptor.capture());
        var cookie = cookieArgumentCaptor.getValue();
        assertEquals(LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME, cookie.getName());
        assertEquals(token, cookie.getValue());
        assertEquals(contextPath.equals("") ? "/" : contextPath, cookie.getPath());
        assertTrue(cookie.getSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    void shouldNotSetCookieWhenSaveContextIsRequestedAndAuthenticationIsMissing(){
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var wrappedResponse = new CookieSecurityContextRepository.SaveToCookieResponseWrapper(request, response, jwtTokenProvider, logger);
        var context = mock(SecurityContext.class);

        when(context.getAuthentication()).thenReturn(null);

        sut.saveContext(context, request, wrappedResponse);

        verify(logger).debug(contains("No user authenticated"));
        verifyNoInteractions(jwtTokenProvider);
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void shouldNotSetCookieWhenSaveContextIsRequestedAndAnonymousUserIsAuthenticated(){
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var wrappedResponse = new CookieSecurityContextRepository.SaveToCookieResponseWrapper(request, response, jwtTokenProvider, logger);
        var context = mock(SecurityContext.class);
        var authentication = mock(Authentication.class);

        when(context.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(LdapAuthWebSecurityConfig.ANONYMOUS_USER);

        sut.saveContext(context, request, wrappedResponse);

        verify(logger).debug(contains("Anonymous User"));
        verifyNoInteractions(jwtTokenProvider);
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void shouldNotSetCookieWhenSaveContextIsRequestedAndPrincipalIsOfUnsupportedType(){
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var wrappedResponse = new CookieSecurityContextRepository.SaveToCookieResponseWrapper(request, response, jwtTokenProvider, logger);
        var context = mock(SecurityContext.class);
        var authentication = mock(Authentication.class);

        when(context.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("invalid-principal");

        sut.saveContext(context, request, wrappedResponse);

        verify(logger).warn(contains("Principal of unsupported type"), anyString());
        verifyNoInteractions(jwtTokenProvider);
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void shouldThrowExceptionWhenSaveContextIsRequestedAndJWTTokenCannotBeCreated(){
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var wrappedResponse = new CookieSecurityContextRepository.SaveToCookieResponseWrapper(request, response, jwtTokenProvider, logger);
        var context = mock(SecurityContext.class);
        var authentication = mock(Authentication.class);
        var userDetails = mock(UserDetails.class);
        var expectedException = mock(JWTTokenCreationFailedException.class);

        when(context.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtTokenProvider.createToken(userDetails)).thenThrow(expectedException);

        try {
            sut.saveContext(context, request, wrappedResponse);
            fail();
        } catch (JWTTokenCreationFailedException e){
            assertSame(expectedException, e);
        }

        verify(jwtTokenProvider).createToken(userDetails);
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void shouldFailToSaveContextWhenResponseIsNotASaveToCookieResponseWrapper(){
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var context = mock(SecurityContext.class);

        assertThrows(ClassCastException.class, () -> sut.saveContext(context, request, response));
    }
}