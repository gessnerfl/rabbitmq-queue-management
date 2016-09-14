package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageChecksum;

@Service
public class MessageDeleteOperation {
    private final Connector connector;
    private final MessageChecksum messageChecksum;

    @Autowired
    public MessageDeleteOperation(Connector connector, MessageChecksum messageChecksum) {
        this.connector = connector;
        this.messageChecksum = messageChecksum;
    }

    public void deleteFirstMessageInQueue(String queue, String messageChekcsum) {
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable()) {
            Channel channel = wrapper.getChannel();
            GetResponse response = channel.basicGet(queue, false);
            String checksum = messageChecksum.createFor(response.getProps(), response.getBody());
            if (messageChekcsum.equals(checksum)) {
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            } else {
                channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
                throw new MessageDeletionFailedException("Checksum does not match");
            }
        } catch (IOException e) {
            throw new MessageDeletionFailedException(e);
        }
    }
}
