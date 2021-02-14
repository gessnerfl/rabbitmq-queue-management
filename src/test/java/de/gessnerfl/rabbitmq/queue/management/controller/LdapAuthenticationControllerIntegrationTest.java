package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.javaconfig.LdapAuthWebSecurityConfig;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.SocketUtils;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtestldap")
@ContextConfiguration(initializers = { LdapAuthenticationControllerIntegrationTest.Initializer.class } )
public class LdapAuthenticationControllerIntegrationTest {

    private static int randomLdapPort = SocketUtils.findAvailableTcpPort(49152, 65535);
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private Filter springSecurityFilterChain;

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of("de.gessnerfl.security.authentication.ldap.contextSource.port=" + randomLdapPort)
                    .applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Before
    public void init(){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilter(springSecurityFilterChain).build();
    }

    @Test
    public void shouldRedirectToLoginWhenUserIsNotLoggedIn() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    public void shouldSuccessfullyLoginUserAndRedirectToIndexAndRedirectToLoginPageAfterSuccessfulLogout() throws Exception {
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
    public void shouldFailToLoginWhenUserCredentialsAreNotValid() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "tester")
                .param("password", "invalid"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login-error"))
                .andExpect(cookie().doesNotExist(LdapAuthWebSecurityConfig.JWT_TOKEN_COOKIE_NAME));
    }
}
