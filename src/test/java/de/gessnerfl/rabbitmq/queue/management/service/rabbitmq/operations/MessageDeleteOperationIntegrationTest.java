package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;

class MessageDeleteOperationIntegrationTest extends AbstractOperationIntegrationTest {
    @Autowired
    private QueueListOperation queueListOperation;
    
    @Autowired
    private MessageDeleteOperation sut;
    
    @Test
    void shouldDeleteFirstMessageInQueue() throws Exception {
        publishMessages(2);
        
        List<Message> firstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 2);
        
        assertThat(firstFetch, hasSize(2));
        
        sut.deleteFirstMessageInQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, firstFetch.get(0).getChecksum());

        List<Message> secondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 2);
        
        assertThat(secondFetch, hasSize(1));
        assertEquals(firstFetch.get(1).getChecksum(), secondFetch.get(0).getChecksum());
    }
    
    @Test
    void shouldFailToDeleteFirstMessageInQueueWhenChecksumDoesNotMacht() throws Exception {
        publishMessages(2);
        
        List<Message> firstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 2);
        
        assertThat(firstFetch, hasSize(2));
        
        try{
            sut.deleteFirstMessageInQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, "invalidChecksum");
            fail("Deletion should fail");
        }catch(MessageOperationFailedException e){
            //expected error
        }

        List<Message> secondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 2);
        
        assertThat(secondFetch, hasSize(2));
        assertEquals(firstFetch.get(0).getChecksum(), secondFetch.get(0).getChecksum());
    }

    @Test
    void shouldFailToDeleteMessageIfMessageWasAlreadyDeletedOrNoMessageExists(){
        List<Message> firstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 2);
        assertThat(firstFetch, empty());

        assertThrows(MessageOperationFailedException.class, () -> {
            sut.deleteFirstMessageInQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, "anyChecksum");
        });
    }
}
