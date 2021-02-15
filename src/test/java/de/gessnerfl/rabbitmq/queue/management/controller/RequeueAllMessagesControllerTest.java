package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequeueAllMessagesControllerTest {

    @Mock
    private RabbitMqFacade facade;
    @Mock
    private Logger logger;

    @InjectMocks
    private RequeueAllMessagesController sut;

    @Test
    void shouldReturnViewWithVhostAndQueueAndTargetExchangeAndTargetRoutingKeyAsParameterOnGet(){
        final String vhost = "vhost";
        final String queue = "queue";
        final String targetExchange = "targetExchange";
        final String targetRoutingKey = "targetRoutingKey";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        final Message.RequeueDetails requeueDetails = mock(Message.RequeueDetails.class);
        final Message message = mock(Message.class);

        when(requeueDetails.getExchangeName()).thenReturn(targetExchange);
        when(requeueDetails.getRoutingKey()).thenReturn(targetRoutingKey);
        when(message.getRequeueDetails()).thenReturn(requeueDetails);
        when(message.isRequeueAllowed()).thenReturn(true);

        when(facade.getMessagesOfQueue(vhost,queue,1)).thenReturn(Collections.singletonList(message));

        String result = sut.getRequeueAllMessagePage(vhost, queue, model, redirectAttributes);

        assertEquals(RequeueAllMessagesController.VIEW_NAME, result);

        verify(facade).getMessagesOfQueue(vhost, queue, 1);
        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.TARGET_EXCHANGE, targetExchange);
        verify(model).addAttribute(Parameters.TARGET_ROUTING_KEY, targetRoutingKey);
        verifyNoInteractions(logger, redirectAttributes);
        verifyNoMoreInteractions(facade, model);
    }

    @Test
    void shouldReturnToMessagePageOnGetWhenNoMessagesAreAvailable(){
        final String vhost = "vhost";
        final String queue = "queue";
        final String targetExchange = "targetExchange";
        final String targetRoutingKey = "targetRoutingKey";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        when(facade.getMessagesOfQueue(vhost,queue,1)).thenReturn(Collections.emptyList());

        String result = sut.getRequeueAllMessagePage(vhost, queue, model, redirectAttributes);

        assertEquals(Pages.MESSAGES.redirectTo(), result);

        verify(facade).getMessagesOfQueue(vhost, queue, 1);
        verify(redirectAttributes).addAttribute(Parameters.VHOST, vhost);
        verify(redirectAttributes).addAttribute(Parameters.QUEUE, queue);
        verifyNoInteractions(logger, model);
        verifyNoMoreInteractions(facade, redirectAttributes);
    }

    @Test
    void shouldReturnToMessagePageOnGetWhenFirstMessageDoesNotSupportRequeuing(){
        final String vhost = "vhost";
        final String queue = "queue";
        final String targetExchange = "targetExchange";
        final String targetRoutingKey = "targetRoutingKey";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        final Message message = mock(Message.class);

        when(message.isRequeueAllowed()).thenReturn(false);
        when(facade.getMessagesOfQueue(vhost,queue,1)).thenReturn(Collections.singletonList(message));

        String result = sut.getRequeueAllMessagePage(vhost, queue, model, redirectAttributes);

        assertEquals(Pages.MESSAGES.redirectTo(), result);

        verify(facade).getMessagesOfQueue(vhost, queue, 1);
        verify(redirectAttributes).addAttribute(Parameters.VHOST, vhost);
        verify(redirectAttributes).addAttribute(Parameters.QUEUE, queue);
        verifyNoInteractions(logger, model);
        verifyNoMoreInteractions(facade, redirectAttributes);
    }

    @Test
    void shouldRequeueAllMessagesAndRedirectToMessagesPageWhenRequeueWasSuccessful(){
        final String vhost = "vhost";
        final String queue = "queue";
        final String targetExchange = "targetExchange";
        final String targetRoutingKey = "targetRoutingKey";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String result = sut.requeueAllMessages(vhost, queue, targetExchange, targetRoutingKey, model, redirectAttributes);

        assertEquals(Pages.MESSAGES.redirectTo(), result);

        verify(facade).requeueAllMessagesInQueue(vhost, queue);
        verify(redirectAttributes).addAttribute(Parameters.VHOST, vhost);
        verify(redirectAttributes).addAttribute(Parameters.QUEUE, queue);
        verifyNoInteractions(model, logger);
        verifyNoMoreInteractions(facade, redirectAttributes);
    }

    @Test
    void shouldTryToRequeueAllMessagesAndStayOnRequeueAllMessagePageWhenRequeueFailed(){
        final String errorMessage = "error";
        final RuntimeException expectedException = new RuntimeException(errorMessage);
        final String vhost = "vhost";
        final String queue = "queue";
        final String targetExchange = "targetExchange";
        final String targetRoutingKey = "targetRoutingKey";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        doThrow(expectedException).when(facade).requeueAllMessagesInQueue(vhost, queue);

        String result = sut.requeueAllMessages(vhost, queue, targetExchange, targetRoutingKey, model, redirectAttributes);

        assertEquals(RequeueAllMessagesController.VIEW_NAME, result);

        verify(facade).requeueAllMessagesInQueue(vhost, queue);
        verify(logger).error(anyString(), eq(queue), eq(vhost), eq(targetExchange), eq(targetRoutingKey), eq(expectedException));
        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.TARGET_EXCHANGE, targetExchange);
        verify(model).addAttribute(Parameters.TARGET_ROUTING_KEY, targetRoutingKey);
        verify(model).addAttribute(Parameters.ERROR_MESSAGE, errorMessage);
        verifyNoInteractions(redirectAttributes);
        verifyNoMoreInteractions(facade, model, logger);
    }
}