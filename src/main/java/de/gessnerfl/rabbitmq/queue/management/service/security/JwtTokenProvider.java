package de.gessnerfl.rabbitmq.queue.management.service.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.ParseException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JwtTokenProvider {

    public static final String CLAIM_NAME_ROLES = "roles";
    private final JWTConfig jwtConfig;
    private JWSAlgorithm cachedJwsAlgorithm;

    public JwtTokenProvider(JWTConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String createToken(UserDetails user) {
        try {
            Date now = new Date();
            Date expirationTime = new Date(now.getTime() + jwtConfig.getToken().getValidity().toMillis());
            JWSSigner signer = new MACSigner(jwtConfig.getToken().getSigningKey());
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer(jwtConfig.getToken().getIssuer())
                    .audience(jwtConfig.getToken().getAudience())
                    .issueTime(now)
                    .expirationTime(expirationTime)
                    .claim(CLAIM_NAME_ROLES, user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(getJwsAlgorithm()), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        }catch (JOSEException e){
            throw new JwtTokenCreationFailedException("Failed to create JWT token", e);
        }
    }

    JWSAlgorithm getJwsAlgorithm(){
        if(cachedJwsAlgorithm == null){
            cachedJwsAlgorithm = determineJwsAlgorithmFromKey();
        }
        return cachedJwsAlgorithm;
    }

    private JWSAlgorithm determineJwsAlgorithmFromKey(){
        var keyLength = jwtConfig.getToken().getSigningKey().length();

        if(keyLength < 32){
            throw new InvalidJwtSigningKeyException();
        }else if(keyLength < 48){
            return JWSAlgorithm.HS256;
        } else if (keyLength < 64) {
            return JWSAlgorithm.HS384;
        } else {
            return JWSAlgorithm.HS512;
        }
    }

    public UserDetails getUserDetailsFromToken(String token) {
        try {
            var jwt = parseAndVerifyToken(token);
            var claimsSet = jwt.getJWTClaimsSet();
            return new User(claimsSet.getSubject(), "", claimsSet.getStringListClaim(CLAIM_NAME_ROLES).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        } catch (ParseException e) {
            throw new InvalidJwtTokenException("Failed to parse roles form JWT claim set", e);
        }
    }

    SignedJWT parseAndVerifyToken(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            var claimSet = jwt.getJWTClaimsSet();

            verifySignature(jwt);
            verifyClaims(claimSet);
            return jwt;
        }catch (ParseException e) {
            throw new BadCredentialsException("Failed to parse JWT token; token is not valid", e);
        }
    }

    private void verifySignature(SignedJWT jwt) {
        try {
            var verifier = new MACVerifier(jwtConfig.getToken().getSigningKey());
            if(!jwt.verify(verifier)){
                throw new BadCredentialsException("Signature of JWT token not valid");
            }
        } catch (JOSEException e) {
            throw new InvalidJwtSigningKeyException();
        }
    }

    private void verifyClaims(JWTClaimsSet claimsSet) {
        String issuer = jwtConfig.getToken().getIssuer();
        String audience = jwtConfig.getToken().getAudience();
        var verifier = new DefaultJWTClaimsVerifier(audience, new JWTClaimsSet.Builder().issuer(issuer).build(), new HashSet<>(Arrays.asList("exp", "sub", CLAIM_NAME_ROLES)));
        try {
            verifier.verify(claimsSet);
        } catch (BadJWTException e) {
            throw new BadCredentialsException("Expired or invalid JWT token", e);
        }
    }
}
