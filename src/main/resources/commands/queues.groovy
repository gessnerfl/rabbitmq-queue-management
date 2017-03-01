package commands

import java.util.List

import org.crsh.cli.Command
import org.crsh.cli.Option;
import org.crsh.cli.Required;
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue
import de.gessnerfl.rabbitmq.queue.management.service.console.ConsoleUtil;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;

@Usage("List queues, queue bindings, queue messages, move and delete first message of queue")
class queues {

    @Usage("List all available queues of the given virtual host")
    @Command
    def ls(@Usage("The broker name") @Option(names=["b", "broker"]) @Required String broker,
           InvocationContext context) {
        List<Queue> queues = getFacade(context).getQueues(broker)
        getConsoleUtil(context).render(context, queues)
    }

    @Usage("List all messages of the given queue within the given virtual host")
    @Command
    def msgs(@Usage("The broker name") @Option(names=["b", "broker"]) @Required String broker,
             @Usage("The queue name") @Option(names=["q", "queue"]) @Required String queue, 
             @Usage("Max number of messages") @Option(names=["l", "limit"]) Integer limit, 
             InvocationContext context) {
        int l = limit != null ? limit : 5
        List<Message> messages = getFacade(context).getMessagesOfQueue(broker, queue, l)
        getConsoleUtil(context).render(context, messages)
    }
    
    @Usage("Deletes the first message from the queue")
    @Command
    def rmf(@Usage("The broker name") @Option(names=["b", "broker"]) @Required String broker,
            @Usage("Name of the queue contain the message") @Option(names=["q", "queue"]) @Required String queue,
            @Usage("The checksum of the message") @Option(names=["c", "checksum"]) @Required String checksum,
            InvocationContext context) {
        getFacade(context).deleteFirstMessageInQueue(broker, queue, checksum);
    }

    @Usage("Move the first message from the queue to a target exchange and routing key")
    @Command
    def mvf(@Usage("The broker name") @Option(names=["b", "broker"]) @Required String broker,
            @Usage("Name of the queue contain the message") @Option(names=["q", "queue"]) @Required String queue,
            @Usage("The checksum of the message") @Option(names=["c", "checksum"]) @Required String checksum,
            @Usage("The target exchange name") @Option(names=["e", "exchange"]) @Required String exchange,
            @Usage("The routing key used to publishing of the message") @Option(names=["r", "routingkey"]) @Required String routingKey,
            InvocationContext context) {
        getFacade(context).moveFirstMessageInQueue(broker, queue, checksum, exchange, routingKey);
    }

    @Usage("List all bindings of the given queue within the given virtual host")
    @Command
    def binds(@Usage("The broker name") @Option(names=["b", "broker"]) @Required String broker,
              @Usage("The queue name") @Option(names=["q", "queue"]) String queue, 
              InvocationContext context) {
        List<Binding> bindings = getFacade(context).getQueueBindings(broker, queue)
        getConsoleUtil(context).render(context, bindings)
    }
    
    private ConsoleUtil getConsoleUtil(InvocationContext context){
        BeanFactory beans = context.attributes['spring.beanfactory']
        return beans.getBean(ConsoleUtil.class)
    }
    
    private RabbitMqFacade getFacade(InvocationContext context){
        BeanFactory beans = context.attributes['spring.beanfactory']
        return beans.getBean(RabbitMqFacade.class)
    }
}