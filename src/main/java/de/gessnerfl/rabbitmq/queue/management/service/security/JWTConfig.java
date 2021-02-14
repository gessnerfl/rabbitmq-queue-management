package de.gessnerfl.rabbitmq.queue.management.service.security;

import org.hibernate.validator.constraints.Length;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

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

        public String getSecretKeyBase64Encoded() {
            return Base64.getEncoder().encodeToString(signingKey.getBytes(StandardCharsets.UTF_8));
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
