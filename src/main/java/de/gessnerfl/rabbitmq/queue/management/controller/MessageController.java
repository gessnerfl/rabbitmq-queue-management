package de.gessnerfl.rabbitmq.queue.management.controller;


import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MessageController {
    private final RabbitMqFacade facade;
    public static final int DEFAULT_LIMIT = 10;

    @Autowired
    public MessageController(RabbitMqFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/messages")
    public String getMessagePage(@RequestParam("vhost") String vhost,
                                 @RequestParam("queue") String queue,
                                 Model model){
        model.addAttribute("vhost", vhost);
        model.addAttribute("queue", queue);
        model.addAttribute("messages", facade.getMessagesOfQueue(vhost, queue, DEFAULT_LIMIT));
        return "messages";
    }

    @GetMapping("/messages/delete-all")
    public String getDeleteAllMessagePage(@RequestParam("vhost") String vhost,
                                          @RequestParam("queue") String queue,
                                          Model model){
        model.addAttribute("vhost", vhost);
        model.addAttribute("queue", queue);
        return "messages-delete-all";
    }

    @PostMapping("/messages/delete-all")
    public String deleteAllMessages(@RequestParam("vhost") String vhost,
                                    @RequestParam("queue") String queue,
                                    Model model,
                                    RedirectAttributes redirectAttributes){
        try {
            facade.purgeQueue(vhost, queue);
            redirectAttributes.addAttribute("vhost", vhost);
            redirectAttributes.addAttribute("queue", queue);
            return "redirect:/messages";
        } catch (Exception e) {
            model.addAttribute("vhost", vhost);
            model.addAttribute("queue", queue);
            model.addAttribute("errorMessage", e.getMessage());
            return "messages-delete-all";
        }
    }

    @GetMapping("/messages/move-all")
    public String getMoveAllMessagePage(@RequestParam("vhost") String vhost,
                                        @RequestParam("queue") String queue,
                                        Model model){
        model.addAttribute("vhost", vhost);
        model.addAttribute("queue", queue);
        model.addAttribute("exchanges", facade.getExchanges(vhost));
        return "messages-move-all";
    }

    @PostMapping("/messages/move-all")
    public String moveAllMessages(@RequestParam("vhost") String vhost,
                                  @RequestParam("queue") String queue,
                                  @RequestParam("targetExchange") String targetExchange,
                                  @RequestParam(name = "targetRoutingKey", required = false) String targetRoutingKey,
                                  Model model,
                                  RedirectAttributes redirectAttributes){
        if(!StringUtils.hasText(targetRoutingKey)){
            List<String> routingKeys = facade.getExchangeSourceBindings(vhost, targetExchange)
                    .stream()
                    .map(Binding::getRoutingKey)
                    .distinct()
                    .collect(Collectors.toList());
            model.addAttribute("vhost", vhost);
            model.addAttribute("queue", queue);
            model.addAttribute("targetExchange", targetExchange);
            model.addAttribute("routingKeys", routingKeys);
            return "messages-move-all";
        }

        try {
            facade.moveAllMessagesInQueue(vhost, queue, targetExchange, targetRoutingKey);
            redirectAttributes.addAttribute("vhost", vhost);
            redirectAttributes.addAttribute("queue", queue);
            return "redirect:/messages";
        } catch (Exception e) {
            model.addAttribute("vhost", vhost);
            model.addAttribute("queue", queue);
            model.addAttribute("targetExchange", targetExchange);
            model.addAttribute("targetRoutingKey", targetRoutingKey);
            model.addAttribute("errorMessage", e.getMessage());
            return "messages-delete-all";
        }
    }

    @GetMapping("/messages/requeue-all")
    public String getRequeueAllMessagePage(@RequestParam("vhost") String vhost,
                                           @RequestParam("queue") String queue,
                                           Model model,
                                           RedirectAttributes redirectAttributes){
        List<Message> messages = facade.getMessagesOfQueue(vhost, queue, 1);
        if(messages.isEmpty() || !messages.get(0).isRequeueAllowed()){
            redirectAttributes.addAttribute("vhost", vhost);
            redirectAttributes.addAttribute("queue", queue);
            return "redirect:/messages";
        }
        Message message = messages.get(0);
        model.addAttribute("vhost", vhost);
        model.addAttribute("queue", queue);
        model.addAttribute("targetExchange", message.getRequeueDetails().getExchangeName());
        model.addAttribute("targetRoutingKey", message.getRequeueDetails().getRoutingKey());
        return "messages-requeue-all";
    }

    @PostMapping("/messages/requeue-all")
    public String requeueAllMessages(@RequestParam("vhost") String vhost,
                                        @RequestParam("queue") String queue,
                                        @RequestParam("targetExchange") String targetExchange,
                                        @RequestParam("targetRoutingKey") String targetRoutingKey,
                                        Model model,
                                        RedirectAttributes redirectAttributes){
        try {
            facade.requeueAllMessagesInQueue(vhost, queue);
            redirectAttributes.addAttribute("vhost", vhost);
            redirectAttributes.addAttribute("queue", queue);
            return "redirect:/messages";
        } catch (Exception e) {
            model.addAttribute("vhost", vhost);
            model.addAttribute("queue", queue);
            model.addAttribute("targetExchange", targetExchange);
            model.addAttribute("targetRoutingKey", targetRoutingKey);
            model.addAttribute("errorMessage", e.getMessage());
            return "messages-delete-all";
        }
    }

    @GetMapping("/messages/delete-first")
    public String getDeleteFirstMessagePage(@RequestParam("vhost") String vhost,
                                            @RequestParam("queue") String queue,
                                            @RequestParam("checksum") String checksum,
                                            Model model){
        model.addAttribute("vhost", vhost);
        model.addAttribute("queue", queue);
        model.addAttribute("checksum", checksum);
        return "messages-delete-first";
    }

    @PostMapping("/messages/delete-first")
    public String deleteFirstMessage(@RequestParam("vhost") String vhost,
                                     @RequestParam("queue") String queue,
                                     @RequestParam("checksum") String checksum,
                                     Model model,
                                     RedirectAttributes redirectAttributes){
        try {
            facade.deleteFirstMessageInQueue(vhost, queue, checksum);
            redirectAttributes.addAttribute("vhost", vhost);
            redirectAttributes.addAttribute("queue", queue);
            return "redirect:/messages";
        } catch (Exception e) {
            model.addAttribute("vhost", vhost);
            model.addAttribute("queue", queue);
            model.addAttribute("checksum", checksum);
            model.addAttribute("errorMessage", e.getMessage());
            return "messages-delete-first";
        }
    }

    @GetMapping("/messages/move-first")
    public String getMoveFirstMessagePage(@RequestParam("vhost") String vhost,
                                          @RequestParam("queue") String queue,
                                          @RequestParam("checksum") String checksum,
                                          Model model){
        model.addAttribute("vhost", vhost);
        model.addAttribute("queue", queue);
        model.addAttribute("checksum", checksum);
        model.addAttribute("exchanges", facade.getExchanges(vhost));
        return "messages-move-first";
    }

    @PostMapping("/messages/move-first")
    public String moveFirstMessage(@RequestParam("vhost") String vhost,
                                   @RequestParam("queue") String queue,
                                   @RequestParam("checksum") String checksum,
                                   @RequestParam("targetExchange") String targetExchange,
                                   @RequestParam(name = "targetRoutingKey", required = false) String targetRoutingKey,
                                   Model model,
                                   RedirectAttributes redirectAttributes){
        if(!StringUtils.hasText(targetRoutingKey)){
            List<String> routingKeys = facade.getExchangeSourceBindings(vhost, targetExchange)
                    .stream()
                    .map(Binding::getRoutingKey)
                    .distinct()
                    .collect(Collectors.toList());
            model.addAttribute("vhost", vhost);
            model.addAttribute("queue", queue);
            model.addAttribute("checksum", checksum);
            model.addAttribute("targetExchange", targetExchange);
            model.addAttribute("routingKeys", routingKeys);
            return "messages-move-first";
        }

        try {
            facade.moveFirstMessageInQueue(vhost, queue, checksum, targetExchange, targetRoutingKey);
            redirectAttributes.addAttribute("vhost", vhost);
            redirectAttributes.addAttribute("queue", queue);
            return "redirect:/messages";
        } catch (Exception e) {
            model.addAttribute("vhost", vhost);
            model.addAttribute("queue", queue);
            model.addAttribute("checksum", checksum);
            model.addAttribute("targetExchange", targetExchange);
            model.addAttribute("targetRoutingKey", targetRoutingKey);
            model.addAttribute("errorMessage", e.getMessage());
            return "messages-delete-first";
        }
    }

    @GetMapping("/messages/requeue-first")
    public String getRequeueFirstMessagePage(@RequestParam("vhost") String vhost,
                                           @RequestParam("queue") String queue,
                                           @RequestParam("checksum") String checksum,
                                           Model model,
                                           RedirectAttributes redirectAttributes){
        List<Message> messages = facade.getMessagesOfQueue(vhost, queue, 1);
        if(messages.isEmpty() || !messages.get(0).isRequeueAllowed()){
            redirectAttributes.addAttribute("vhost", vhost);
            redirectAttributes.addAttribute("queue", queue);
            return "redirect:/messages";
        }
        Message message = messages.get(0);
        model.addAttribute("vhost", vhost);
        model.addAttribute("queue", queue);
        model.addAttribute("checksum", checksum);
        model.addAttribute("targetExchange", message.getRequeueDetails().getExchangeName());
        model.addAttribute("targetRoutingKey", message.getRequeueDetails().getRoutingKey());
        return "messages-requeue-first";
    }

    @PostMapping("/messages/requeue-first")
    public String requeueFirstMessage(@RequestParam("vhost") String vhost,
                                     @RequestParam("queue") String queue,
                                     @RequestParam("checksum") String checksum,
                                     @RequestParam("targetExchange") String targetExchange,
                                     @RequestParam("targetRoutingKey") String targetRoutingKey,
                                     Model model,
                                     RedirectAttributes redirectAttributes){
        try {
            facade.requeueFirstMessageInQueue(vhost, queue, checksum);
            redirectAttributes.addAttribute("vhost", vhost);
            redirectAttributes.addAttribute("queue", queue);
            return "redirect:/messages";
        } catch (Exception e) {
            model.addAttribute("vhost", vhost);
            model.addAttribute("queue", queue);
            model.addAttribute("checksum", checksum);
            model.addAttribute("targetExchange", targetExchange);
            model.addAttribute("targetRoutingKey", targetRoutingKey);
            model.addAttribute("errorMessage", e.getMessage());
            return "messages-delete-first";
        }
    }
}
