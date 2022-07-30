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

public class JWTTokenProvider {

    public static final String CLAIM_NAME_ROLES = "roles";
    private final JWTConfig jwtConfig;
    private JWSAlgorithm cachedJwsAlgorithm;

    public JWTTokenProvider(JWTConfig jwtConfig) {
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
                    .claim(CLAIM_NAME_ROLES, user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(getJwsAlgorithm()), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        }catch (JOSEException e){
            throw new JWTTokenCreationFailedException("Failed to create JWT token", e);
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
            throw new InvalidJWTSigningKeyException();
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
            return new User(claimsSet.getSubject(), "", claimsSet.getStringListClaim(CLAIM_NAME_ROLES).stream().map(SimpleGrantedAuthority::new).toList());
        } catch (ParseException e) {
            throw new InvalidJWTTokenException("Failed to parse roles form JWT claim set", e);
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
            throw new InvalidJWTSigningKeyException();
        }
    }

    private void verifyClaims(JWTClaimsSet claimsSet) {
        String issuer = jwtConfig.getToken().getIssuer();
        String audience = jwtConfig.getToken().getAudience();
        var verifier = new DefaultJWTClaimsVerifier(audience, new JWTClaimsSet.Builder().issuer(issuer).build(), new HashSet<>(Arrays.asList("exp", "sub", CLAIM_NAME_ROLES)));
        try {
            verifier.verify(claimsSet, null);
        } catch (BadJWTException e) {
            throw new BadCredentialsException("Expired or invalid JWT token", e);
        }
    }
}
