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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteFirstMessageControllerTest {

    @Mock
    private RabbitMqFacade facade;
    @Mock
    private Logger logger;

    @InjectMocks
    private DeleteFirstMessageController sut;

    @Test
    void shouldReturnMainViewWithVhostAndQueueAndChecksumAsParameterOnGet(){
        final String vhost = "vhost";
        final String queue = "queue";
        final String checksum = "checksum";
        final Model model = mock(Model.class);

        String result = sut.getDeleteFirstMessagePage(vhost, queue, checksum, model);

        assertEquals(DeleteFirstMessageController.VIEW_NAME, result);

        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.CHECKSUM, checksum);
        verifyNoInteractions(facade, logger);
    }

    @Test
    void shouldDeleteFirstMessagesAndRedirectToMessagesPageWhenDeletionWasSuccessful(){
        final String vhost = "vhost";
        final String queue = "queue";
        final String checksum = "checksum";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String result = sut.deleteFirstMessage(vhost, queue, checksum, model, redirectAttributes);

        assertEquals(Pages.MESSAGES.getRedirectString(), result);

        verify(facade).deleteFirstMessageInQueue(vhost, queue, checksum);
        verify(redirectAttributes).addAttribute(Parameters.VHOST, vhost);
        verify(redirectAttributes).addAttribute(Parameters.QUEUE, queue);
        verifyNoInteractions(model, logger);
        verifyNoMoreInteractions(facade, redirectAttributes);
    }

    @Test
    void shouldTryToDeleteFirstMessagesAndStayOnDeleteFirstMessagePageWhenDeletionFailed(){
        final String errorMessage = "error";
        final RuntimeException expectedException = new RuntimeException(errorMessage);
        final String vhost = "vhost";
        final String queue = "queue";
        final String checksum = "checksum";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        doThrow(expectedException).when(facade).deleteFirstMessageInQueue(vhost, queue, checksum);

        String result = sut.deleteFirstMessage(vhost, queue, checksum, model, redirectAttributes);

        assertEquals(DeleteFirstMessageController.VIEW_NAME, result);

        verify(facade).deleteFirstMessageInQueue(vhost, queue, checksum);
        verify(logger).error(anyString(), eq(checksum), eq(queue), eq(vhost), eq(expectedException));
        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.CHECKSUM, checksum);
        verify(model).addAttribute(Parameters.ERROR_MESSAGE, errorMessage);
        verifyNoInteractions(redirectAttributes);
        verifyNoMoreInteractions(facade, model, logger);
    }
}