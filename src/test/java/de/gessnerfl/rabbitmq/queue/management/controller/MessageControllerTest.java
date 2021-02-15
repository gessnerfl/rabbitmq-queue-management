package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageControllerTest {

    @Mock
    private RabbitMqFacade facade;

    @InjectMocks
    private MessageController sut;

    @Test
    public void shouldReturnViewWithMessagesProvidedInModel(){
        final String vhost = "vhost";
        final String queue = "queue";
        final Model model = mock(Model.class);
        final Message message = mock(Message.class);
        final List<Message> messageList = Arrays.asList(message, message);

        when(facade.getMessagesOfQueue(vhost, queue, MessageController.DEFAULT_LIMIT)).thenReturn(messageList);

        String result = sut.getMessagePage(vhost, queue, model);

        assertEquals(MessageController.VIEW_NAME, result);

        verify(facade).getMessagesOfQueue(vhost, queue, MessageController.DEFAULT_LIMIT);
        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.MESSAGES, messageList);
        verifyNoMoreInteractions(model, facade);
    }

}