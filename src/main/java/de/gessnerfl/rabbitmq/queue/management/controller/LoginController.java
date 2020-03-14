package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.service.security.AuthenticationConfig;
import de.gessnerfl.rabbitmq.queue.management.service.security.SecurityContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class LoginController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    private final SecurityContextService securityContextService;
    private final AuthenticationConfig authenticationConfig;

    @Autowired
    public LoginController(SecurityContextService securityContextService, AuthenticationConfig authenticationConfig) {
        this.securityContextService = securityContextService;
        this.authenticationConfig = authenticationConfig;
    }

    @GetMapping("/login")
    public String getLogin(HttpServletRequest request) {
        if (!authenticationConfig.isEnabled()){
            LOGGER.info("Authentication not enabled. Redirect to index page");
            return "redirect:/index";
        }
        if (securityContextService.isUserAuthenticated()) {
            LOGGER.info("User is already authenticated. Redirect to index page");
            return "redirect:/index";
        }
        return "login";
    }

    @GetMapping("/login-error")
    public String showLoginError(Model model, HttpServletRequest request) {
        model.addAttribute("error", true);
        model.addAttribute("login_error_msg", "Invalid username or password or insufficient permissions");
        return "login";
    }
}
