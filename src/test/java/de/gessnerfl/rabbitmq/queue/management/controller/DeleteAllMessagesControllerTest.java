package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteAllMessagesControllerTest {

    @Mock
    private RabbitMqFacade facade;
    @Mock
    private Logger logger;

    @InjectMocks
    private DeleteAllMessagesController sut;

    @Test
    public void shouldReturnMainViewWithVhostAndQueueAsParameterOnGet(){
        final String vhost = "vhost";
        final String queue = "queue";
        final Model model = mock(Model.class);

        String result = sut.getDeleteAllMessagePage(vhost, queue, model);

        assertEquals(DeleteAllMessagesController.VIEW_NAME, result);

        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verifyNoInteractions(facade, logger);
    }

    @Test
    public void shouldDeleteAllMessagesAndRedirectToMessagesPageWhenDeletionWasSuccessful(){
        final String vhost = "vhost";
        final String queue = "queue";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String result = sut.deleteAllMessages(vhost, queue, model, redirectAttributes);

        assertEquals(Pages.MESSAGES.redirectTo(), result);

        verify(facade).purgeQueue(vhost, queue);
        verify(redirectAttributes).addAttribute(Parameters.VHOST, vhost);
        verify(redirectAttributes).addAttribute(Parameters.QUEUE, queue);
        verifyNoInteractions(model, logger);
        verifyNoMoreInteractions(facade, redirectAttributes);
    }

    @Test
    public void shouldTryToDeleteAllMessagesAndStayOnDeleteAllMessagePageWhenDeletionFailed(){
        final String errorMessage = "error";
        final RuntimeException expectedException = new RuntimeException(errorMessage);
        final String vhost = "vhost";
        final String queue = "queue";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        doThrow(expectedException).when(facade).purgeQueue(vhost, queue);

        String result = sut.deleteAllMessages(vhost, queue, model, redirectAttributes);

        assertEquals(DeleteAllMessagesController.VIEW_NAME, result);

        verify(facade).purgeQueue(vhost, queue);
        verify(logger).error(anyString(), eq(queue), eq(vhost), eq(expectedException));
        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.ERROR_MESSAGE, errorMessage);
        verifyNoInteractions(redirectAttributes);
        verifyNoMoreInteractions(facade, model, logger);
    }
}