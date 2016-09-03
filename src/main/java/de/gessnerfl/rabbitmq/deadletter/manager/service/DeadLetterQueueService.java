package de.gessnerfl.rabbitmq.deadletter.manager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.deadletter.manager.model.Message;
import de.gessnerfl.rabbitmq.deadletter.manager.repository.MessageRepository;

@Service
public class DeadLetterQueueService {
  
  private final MessageRepository messageRepository;
  
  public DeadLetterQueueService(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  public List<Message> getMessages(String queueName, int maxNumberOfMessages) {
    return messageRepository.getMessagesFromQueue(queueName, maxNumberOfMessages);
  }

}
