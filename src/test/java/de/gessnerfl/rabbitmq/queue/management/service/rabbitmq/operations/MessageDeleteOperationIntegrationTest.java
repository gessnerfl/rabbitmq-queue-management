package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations.MessageDeleteOperation;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations.MessageOperationFailedException;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations.QueueListOperation;

public class MessageDeleteOperationIntegrationTest extends AbstractOperationIntegrationTest {
    @Autowired
    private QueueListOperation queueListOperation;
    
    @Autowired
    private MessageDeleteOperation sut;
    
    @Test
    public void shouldDeleteFirstMessageInQueue() throws Exception {
        publishMessages(2);
        
        List<Message> firstFetch = queueListOperation.getMessagesFromQueue(QUEUE_NAME, 2);
        
        assertThat(firstFetch, hasSize(2));
        
        sut.deleteFirstMessageInQueue(QUEUE_NAME, firstFetch.get(0).getChecksum());

        List<Message> secondFetch = queueListOperation.getMessagesFromQueue(QUEUE_NAME, 2);
        
        assertThat(secondFetch, hasSize(1));
        assertEquals(firstFetch.get(1).getChecksum(), secondFetch.get(0).getChecksum());
    }
    
    @Test
    public void shouldFailToDeleteFirstMessageInQueueWhenChecksumDoesNotMacht() throws Exception {
        publishMessages(2);
        
        List<Message> firstFetch = queueListOperation.getMessagesFromQueue(QUEUE_NAME, 2);
        
        assertThat(firstFetch, hasSize(2));
        
        try{
            sut.deleteFirstMessageInQueue(QUEUE_NAME, "invalidChecksum");
            fail("Deletion should fail");
        }catch(MessageOperationFailedException e){
            //expected error
        }

        List<Message> secondFetch = queueListOperation.getMessagesFromQueue(QUEUE_NAME, 2);
        
        assertThat(secondFetch, hasSize(2));
        assertEquals(firstFetch.get(0).getChecksum(), secondFetch.get(0).getChecksum());
    }

    @Test(expected=MessageOperationFailedException.class)
    public void shouldFailToDeleteMessageIfMessageWasAlreadyDeletedOrNoMessageExists(){
        List<Message> firstFetch = queueListOperation.getMessagesFromQueue(QUEUE_NAME, 2);
        assertThat(firstFetch, empty());
        
        sut.deleteFirstMessageInQueue(QUEUE_NAME, "anyChecksum");
    }
}
