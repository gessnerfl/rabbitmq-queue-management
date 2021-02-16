package de.gessnerfl.rabbitmq.queue.management.service.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;

class JwtTokenProviderTest {

    private static final String SIGNING_KEY_512BIT = "Ymnj2HMDpax#TY2zpnYBFnHedDwpN%Wpn7%%p^STRnVetsE8bsA4zxcze%8YnSNU";
    private static final String SIGNING_KEY_384BIT = "qa2VAe4amG8FtKJ!tnPt6sz&b6cQjm&vAcc8rK^b&v&mx538";
    private static final String SIGNING_KEY_256BIT = "359@RyEbH*hjDqYgK6psQXdcycJEQP9q";

    private static final String ISSUER = "issuer";
    private static final String AUDIENCE = "audience";
    private static final Duration VALIDITY = Duration.ofMinutes(1);
    private static final String USERNAME = "user@example.com";
    private static final String ROLE_1 = "role1";
    private static final String ROLE_2 = "role2";
    private static final Collection ROLES = Arrays.asList(new SimpleGrantedAuthority(ROLE_1), new SimpleGrantedAuthority(ROLE_2));
    private static final String EXPIRED_OR_INVALID_JWT_TOKEN_MESSAGE = "Expired or invalid JWT token";

    private JWTConfig.JwtTokenConfig jwtTokenConfig;

    private JwtTokenProvider sut;

    @BeforeEach
    void init(){
        jwtTokenConfig = mock(JWTConfig.JwtTokenConfig.class);
        var config = new JWTConfig();
        config.setToken(jwtTokenConfig);

        sut = new JwtTokenProvider(config);
    }

    @Test
    void shouldSuccessfullyDetermineJwsAlgorithmForSigningFor256BitKey() {
        shouldSuccessfullyDetermineJwsAlgorithmForSigning(SIGNING_KEY_256BIT, JWSAlgorithm.HS256);
    }

    @Test
    void shouldSuccessfullyDetermineJwsAlgorithmForSigningFor384BitKey() {
        shouldSuccessfullyDetermineJwsAlgorithmForSigning(SIGNING_KEY_384BIT, JWSAlgorithm.HS384);
    }

    @Test
    void shouldSuccessfullyDetermineJwsAlgorithmForSigningFor512BitKey() {
        shouldSuccessfullyDetermineJwsAlgorithmForSigning(SIGNING_KEY_512BIT, JWSAlgorithm.HS512);
    }

    private void shouldSuccessfullyDetermineJwsAlgorithmForSigning(String signingKey, JWSAlgorithm expectedAlgorithm) {
        when(jwtTokenConfig.getSigningKey()).thenReturn(signingKey);

        assertEquals(expectedAlgorithm, sut.getJwsAlgorithm());
    }

    @Test
    void shouldFailToDetermineJwsAlgorithmForSigningWhenSigningKeyIsTooShort(){
        when(jwtTokenConfig.getSigningKey()).thenReturn(RandomStringUtils.randomAlphanumeric(31));

        assertThrows(InvalidJwtSigningKeyException.class, () ->  sut.getJwsAlgorithm());
    }

    @ParameterizedTest
    @ValueSource(strings = {SIGNING_KEY_256BIT, SIGNING_KEY_384BIT, SIGNING_KEY_512BIT})
    void shouldSuccessfullyCreateAndReadToken(String signingKey) throws Exception {
        var userDetails = mockDefaultUserDetails();
        mockDefaultConfig(signingKey);

        var token = sut.createToken(userDetails);

        assertNotNull(token);

        var jwt = sut.parseAndVerifyToken(token);
        var claims = jwt.getJWTClaimsSet();

        assertEquals(ISSUER, claims.getIssuer());
        assertThat(claims.getAudience(), contains(AUDIENCE));
        assertThat(claims.getExpirationTime(), greaterThan(new Date()));
        assertThat(claims.getStringListClaim(JwtTokenProvider.CLAIM_NAME_ROLES), containsInAnyOrder(ROLE_1, ROLE_2));
    }

    @Test
    void shouldFailToCreateTokenWhenVSigningFails(){
        var userDetails = mockDefaultUserDetails();
        mockDefaultConfig(SIGNING_KEY_256BIT);

        var sut = spy(this.sut);
        doReturn(JWSAlgorithm.HS512).when(sut).getJwsAlgorithm();

        assertThrows(JwtTokenCreationFailedException.class, () -> sut.createToken(userDetails));
    }

    @ParameterizedTest
    @ValueSource(strings = {SIGNING_KEY_256BIT, SIGNING_KEY_384BIT, SIGNING_KEY_512BIT})
    void shouldExtractUserDetailsFromToken(String signingKey){
        var userDetails = mockDefaultUserDetails();
        mockDefaultConfig(signingKey);

        var token = sut.createToken(userDetails);

        assertNotNull(token);

        var result = sut.getUserDetailsFromToken(token);

        assertEquals(USERNAME, result.getUsername());
        assertEquals("", result.getPassword());
        assertThat(result.getAuthorities(), containsInAnyOrder(new SimpleGrantedAuthority(ROLE_1), new SimpleGrantedAuthority(ROLE_2)));
    }

    @Test
    void shouldExtractUserDetailsFromTokenWhenUserDoesNotHaveAnyRolesAssigned(){
        var userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(USERNAME);
        mockDefaultConfig(SIGNING_KEY_256BIT);

        var token = sut.createToken(userDetails);

        assertNotNull(token);

        var result = sut.getUserDetailsFromToken(token);

        assertEquals(USERNAME, result.getUsername());
        assertEquals("", result.getPassword());
        assertThat(result.getAuthorities(), empty());
    }

    @Test
    void shouldFailToExtractUserDetailsWhenRolesAreNotAStringList() throws Exception{
        var signingKey = SIGNING_KEY_256BIT;
        mockDefaultConfig(signingKey);

        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + VALIDITY.toMillis());
        JWSSigner signer = new MACSigner(signingKey);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(USERNAME)
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .issueTime(now)
                .expirationTime(expirationTime)
                .claim(JwtTokenProvider.CLAIM_NAME_ROLES, "invalid")
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);

        var token = signedJWT.serialize();

        var sut = spy(this.sut);
        doReturn(signedJWT).when(sut).parseAndVerifyToken(token);

        try {
            sut.getUserDetailsFromToken(token);
            fail();
        } catch (InvalidJwtTokenException e) {
            assertThat(e.getMessage(), containsString("Failed to parse roles"));
        }
    }

    @Test
    void shouldFailToVerifyTokenWhenTokenIsNotValid(){
        try {
            sut.parseAndVerifyToken("invalid-token");
            fail();
        } catch (BadCredentialsException e){
            assertThat(e.getMessage(), containsString("Failed to parse JWT token"));
        }
    }

    @Test
    void shouldFailToVerifyTokenWhenTokenWasSignedWithADifferentKey() {
        when(jwtTokenConfig.getSigningKey()).thenReturn(SIGNING_KEY_256BIT, SIGNING_KEY_256BIT, SIGNING_KEY_512BIT);
        when(jwtTokenConfig.getAudience()).thenReturn(AUDIENCE);
        when(jwtTokenConfig.getIssuer()).thenReturn(ISSUER);
        when(jwtTokenConfig.getValidity()).thenReturn(VALIDITY);

        shouldFailToValidateToken("Signature of JWT token not valid");
    }

    @Test
    void shouldFailToVerifyTokenWhenTokenWasIssuedByADifferentIssuer(){
        when(jwtTokenConfig.getSigningKey()).thenReturn(SIGNING_KEY_256BIT);
        when(jwtTokenConfig.getAudience()).thenReturn(AUDIENCE);
        when(jwtTokenConfig.getIssuer()).thenReturn(ISSUER, "other-issuer");
        when(jwtTokenConfig.getValidity()).thenReturn(VALIDITY);

        shouldFailToValidateToken(EXPIRED_OR_INVALID_JWT_TOKEN_MESSAGE);
    }

    @Test
    void shouldFailToVerifyTokenWhenTokenWasIssuedForADifferentAudience(){
        when(jwtTokenConfig.getSigningKey()).thenReturn(SIGNING_KEY_256BIT);
        when(jwtTokenConfig.getAudience()).thenReturn(AUDIENCE, "other-audience");
        when(jwtTokenConfig.getIssuer()).thenReturn(ISSUER);
        when(jwtTokenConfig.getValidity()).thenReturn(VALIDITY);

        shouldFailToValidateToken(EXPIRED_OR_INVALID_JWT_TOKEN_MESSAGE);
    }

    @Test
    void shouldFailToVerifyTokenWhenTokenIsExpired(){
        when(jwtTokenConfig.getSigningKey()).thenReturn(SIGNING_KEY_256BIT);
        when(jwtTokenConfig.getAudience()).thenReturn(AUDIENCE, "other-audience");
        when(jwtTokenConfig.getIssuer()).thenReturn(ISSUER);
        when(jwtTokenConfig.getValidity()).thenReturn(Duration.ZERO);

        shouldFailToValidateToken(EXPIRED_OR_INVALID_JWT_TOKEN_MESSAGE);
    }

    private void shouldFailToValidateToken(String expectedMessage) {
        var userDetails = mockDefaultUserDetails();
        var token = sut.createToken(userDetails);

        assertNotNull(token);

        try {
            sut.parseAndVerifyToken(token);
            fail();
        } catch (BadCredentialsException e) {
            assertThat(e.getMessage(), containsString(expectedMessage));
        }
    }

    @Test
    void shouldFailToVerifyTokenWhenTokenDoesNotContainRoleClaim() throws Exception {
        var signingKey = SIGNING_KEY_256BIT;
        mockDefaultConfig(signingKey);

        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + VALIDITY.toMillis());
        JWSSigner signer = new MACSigner(signingKey);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(USERNAME)
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .issueTime(now)
                .expirationTime(expirationTime)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);

        var token = signedJWT.serialize();

        try {
            sut.parseAndVerifyToken(token);
            fail();
        } catch (BadCredentialsException e) {
            assertThat(e.getMessage(), containsString(EXPIRED_OR_INVALID_JWT_TOKEN_MESSAGE));
        }
    }

    @Test
    void shouldFailToVerifyTokenWhenConfigureSigningKeyIsNotValid(){
        var userDetails = mockDefaultUserDetails();

        when(jwtTokenConfig.getSigningKey()).thenReturn(SIGNING_KEY_256BIT, SIGNING_KEY_256BIT, "invalid-key");
        when(jwtTokenConfig.getAudience()).thenReturn(AUDIENCE);
        when(jwtTokenConfig.getIssuer()).thenReturn(ISSUER);
        when(jwtTokenConfig.getValidity()).thenReturn(VALIDITY);

        var token = sut.createToken(userDetails);

        assertNotNull(token);

        assertThrows(InvalidJwtSigningKeyException.class, () -> sut.parseAndVerifyToken(token));
    }

    private UserDetails mockDefaultUserDetails() {
        var userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(USERNAME);
        when(userDetails.getAuthorities()).thenReturn(ROLES);
        return userDetails;
    }

    private void mockDefaultConfig(String signingKey) {
        when(jwtTokenConfig.getSigningKey()).thenReturn(signingKey);
        when(jwtTokenConfig.getAudience()).thenReturn(AUDIENCE);
        when(jwtTokenConfig.getIssuer()).thenReturn(ISSUER);
        when(jwtTokenConfig.getValidity()).thenReturn(VALIDITY);
    }

}