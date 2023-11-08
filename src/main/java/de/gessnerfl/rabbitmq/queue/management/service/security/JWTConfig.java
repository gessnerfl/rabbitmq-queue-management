package de.gessnerfl.rabbitmq.queue.management.service.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "de.gessnerfl.security.authentication.jwt")
@ConditionalOnProperty(prefix = "de.gessnerfl.security.authentication", name = "enabled", havingValue = "true")
@Validated
public class JWTConfig {

    @Valid
    private JwtTokenConfig token = new JwtTokenConfig();

    public JwtTokenConfig getToken() {
        return token;
    }

    public void setToken(JwtTokenConfig token) {
        this.token = token;
    }

    public static class JwtTokenConfig {
        @Length(min = 32)
        private String signingKey;
        @NotBlank
        private String issuer = "https://rabbitmq-queue-management.service.local";
        @NotBlank
        private String audience = "https://rabbitmq-queue-management-ui.service.local";
        @NotNull
        private Duration validity = Duration.ofHours(1);

        public String getSigningKey() {
            return signingKey;
        }

        public void setSigningKey(String signingKey) {
            this.signingKey = signingKey;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public Duration getValidity() {
            return validity;
        }

        public void setValidity(Duration validity) {
            this.validity = validity;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }
    }
}
