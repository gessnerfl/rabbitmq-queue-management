package de.gessnerfl.rabbitmq.queue.management.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class MessagesControllers {
    public static String redirectToMessagesPage(String vhost, String queue, RedirectAttributes redirectAttributes){
        redirectAttributes.addAttribute(Parameters.VHOST, vhost);
        redirectAttributes.addAttribute(Parameters.QUEUE, queue);
        return Pages.MESSAGES.redirectTo();
    }
}
