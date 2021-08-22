package de.gessnerfl.rabbitmq.queue.management.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class BasicRedirectAttributes {
    public static BasicRedirectAttributes appendTo(RedirectAttributes redirectAttributes) {
        return new BasicRedirectAttributes(redirectAttributes);
    }

    private final RedirectAttributes redirectAttributes;

    private BasicRedirectAttributes(RedirectAttributes redirectAttributes) {
        this.redirectAttributes = redirectAttributes;
    }

    public BasicRedirectAttributes vhost(String vhost) {
        redirectAttributes.addAttribute(Parameters.VHOST, vhost);
        return this;
    }

    public BasicRedirectAttributes queue(String queue) {
        redirectAttributes.addAttribute(Parameters.QUEUE, queue);
        return this;
    }
}
