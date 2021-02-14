package de.gessnerfl.rabbitmq.queue.management.service.security;

import org.junit.Before;
import org.junit.Test;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

public class JwtTokenProviderTest {

    private static final String SIGNING_KEY = RandomStringUtils.randomAlphanumeric(64);
    private JWTConfig jwtConfig;

    private JwtTokenProvider sut;

    @Before
    public void init() {
        jwtConfig = new JWTConfig();
        jwtConfig.getToken().setSigningKey(SIGNING_KEY);
        sut = new JwtTokenProvider(jwtConfig);
    }

    @Test
    public void shouldSuccessfullyCreateToken() {

    }

}