package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoveFirstMessagesControllerTest {

    @Mock
    private RabbitMqFacade facade;
    @Mock
    private Logger logger;

    @InjectMocks
    private MoveFirstMessageController sut;

    @Test
    void shouldReturnViewWithVhostAndQueueAndChecksumAsParameterOnGet(){
        final String vhost = "vhost";
        final String queue = "queue";
        final String checksum = "checksum";
        final Model model = mock(Model.class);
        final Exchange exchange = mock(Exchange.class);
        final List<Exchange> exchangeList = Arrays.asList(exchange, exchange);

        when(facade.getExchanges(vhost)).thenReturn(exchangeList);

        String result = sut.getMoveFirstMessagePage(vhost, queue, checksum, model);

        assertEquals(MoveFirstMessageController.VIEW_NAME, result);

        verify(facade).getExchanges(vhost);
        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.CHECKSUM, checksum);
        verify(model).addAttribute(Parameters.EXCHANGES, exchangeList);
        verifyNoInteractions(logger);
        verifyNoMoreInteractions(facade);
    }

    @Test
    void shouldStayOnPageWhenTargetExchangeIsProvidedAndTargetRoutingKeyIsNotProvided(){
        final String routingKey1 = "routingKey1";
        final String routingKey2 = "routingKey2";
        final Binding binding1 = mock(Binding.class);
        final Binding binding2 = mock(Binding.class);
        final String vhost = "vhost";
        final String queue = "queue";
        final String checksum = "checksum";
        final String targetExchange = "targetExchange";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        when(binding1.getRoutingKey()).thenReturn(routingKey1);
        when(binding2.getRoutingKey()).thenReturn(routingKey2);
        when(facade.getExchangeSourceBindings(vhost, targetExchange)).thenReturn(Arrays.asList(binding1, binding2));

        String result = sut.moveFirstMessage(vhost, queue, checksum, targetExchange, null, model, redirectAttributes);

        assertEquals(MoveFirstMessageController.VIEW_NAME, result);

        verify(facade).getExchangeSourceBindings(vhost, targetExchange);
        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.CHECKSUM, checksum);
        verify(model).addAttribute(Parameters.TARGET_EXCHANGE, targetExchange);
        verify(model).addAttribute(Parameters.ROUTING_KEYS, Arrays.asList(routingKey1, routingKey2));
        verifyNoInteractions(redirectAttributes, logger);
        verifyNoMoreInteractions(facade, model);
    }

    @Test
    void shouldMoveFirstMessagesAndRedirectToMessagesPageWhenTargetExchangeAndRoutingKeyAreProvidedAndMoveWasSuccessful(){
        final String vhost = "vhost";
        final String queue = "queue";
        final String checksum = "checksum";
        final String targetExchange = "targetExchange";
        final String targetRoutingKey = "targetRoutingKey";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String result = sut.moveFirstMessage(vhost, queue, checksum, targetExchange, targetRoutingKey, model, redirectAttributes);

        assertEquals(Pages.MESSAGES.getRedirectString(), result);

        verify(facade).moveFirstMessageInQueue(vhost, queue, checksum, targetExchange, targetRoutingKey);
        verify(redirectAttributes).addAttribute(Parameters.VHOST, vhost);
        verify(redirectAttributes).addAttribute(Parameters.QUEUE, queue);
        verifyNoInteractions(model, logger);
        verifyNoMoreInteractions(facade, redirectAttributes);
    }

    @Test
    void shouldTryToMoveFirstMessagesAndStayOnMoveAllMessagePageWhenTargetExchangeAndRoutingKeyAreProvidedAndMoveFailed(){
        final String errorMessage = "error";
        final RuntimeException expectedException = new RuntimeException(errorMessage);
        final String routingKey1 = "routingKey1";
        final String routingKey2 = "routingKey2";
        final Binding binding1 = mock(Binding.class);
        final Binding binding2 = mock(Binding.class);
        final String vhost = "vhost";
        final String queue = "queue";
        final String checksum = "checksum";
        final String targetExchange = "targetExchange";
        final String targetRoutingKey = "targetRoutingKey";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        when(binding1.getRoutingKey()).thenReturn(routingKey1);
        when(binding2.getRoutingKey()).thenReturn(routingKey2);
        when(facade.getExchangeSourceBindings(vhost, targetExchange)).thenReturn(Arrays.asList(binding1, binding2));

        doThrow(expectedException).when(facade).moveFirstMessageInQueue(vhost, queue, checksum, targetExchange, targetRoutingKey);

        String result = sut.moveFirstMessage(vhost, queue, checksum, targetExchange, targetRoutingKey, model, redirectAttributes);

        assertEquals(MoveFirstMessageController.VIEW_NAME, result);

        verify(facade).moveFirstMessageInQueue(vhost, queue, checksum, targetExchange, targetRoutingKey);
        verify(facade).getExchangeSourceBindings(vhost, targetExchange);
        verify(logger).error(anyString(), eq(checksum), eq(queue), eq(vhost), eq(targetExchange), eq(targetRoutingKey),  eq(expectedException));
        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.CHECKSUM, checksum);
        verify(model).addAttribute(Parameters.TARGET_EXCHANGE, targetExchange);
        verify(model).addAttribute(Parameters.TARGET_ROUTING_KEY, targetRoutingKey);
        verify(model).addAttribute(Parameters.ROUTING_KEYS, Arrays.asList(routingKey1, routingKey2));
        verify(model).addAttribute(Parameters.ERROR_MESSAGE, errorMessage);
        verifyNoInteractions(redirectAttributes);
        verifyNoMoreInteractions(facade, model, logger);
    }
}