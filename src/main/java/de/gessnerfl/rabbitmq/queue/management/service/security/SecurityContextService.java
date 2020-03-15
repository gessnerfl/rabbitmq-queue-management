package de.gessnerfl.rabbitmq.queue.management.service.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityContextService {

    public boolean isUserAuthenticated(){
        return getAuthentication().filter(this::isNotAnonymousAuthentication).map(Authentication::isAuthenticated).orElse(false);
    }

    private boolean isNotAnonymousAuthentication(Authentication authentication){
        return !(authentication instanceof AnonymousAuthenticationToken);
    }

    private Optional<Authentication> getAuthentication(){
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }
}
