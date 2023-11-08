package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.javaconfig.LdapAuthWebSecurityConfig;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtestldap")
@ContextConfiguration(initializers = { LdapAuthenticationControllerIntegrationTest.Initializer.class } )
class LdapAuthenticationControllerIntegrationTest {

    private static final int RANDOM_LDAP_PORT = TestSocketUtils.findAvailableTcpPort();
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private Filter springSecurityFilterChain;

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of("de.gessnerfl.security.authentication.ldap.contextSource.port=" + RANDOM_LDAP_PORT)
                    .applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @BeforeEach
    void init(){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilter(springSecurityFilterChain).build();
    }

    @Test
    void shouldRedirectToLoginWhenUserIsNotLoggedIn() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/login?target-url=/index"));
    }

    @Test
    void shouldSuccessfullyLoginUserAndRedirectToIndexAndRedirectToLoginPageAfterSuccessfulLogout() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "tester")
                .param("password", "tester"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().exists(LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME));

        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(cookie().maxAge(LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME, 0));
    }

    @Test
    void shouldFailToLoginWhenUserCredentialsAreNotValid() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "tester")
                .param("password", "invalid"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login-error"))
                .andExpect(cookie().doesNotExist(LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME));
    }
}
