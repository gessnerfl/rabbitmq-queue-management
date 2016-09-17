package commands

import java.util.List

import org.crsh.cli.Command
import org.crsh.cli.Option;
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.junit.After
import org.springframework.beans.factory.BeanFactory;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange
import de.gessnerfl.rabbitmq.queue.management.service.console.ConsoleUtil;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;

@Usage("List exchanges and source bindings of exchanges")
class exchanges {

    @Usage("List all available exchanges of the given virtual host")
    @Command
    def ls(InvocationContext context) {
        List<Exchange> exchanges = getFacade(context).getExchanges()
        getConsoleUtil(context).render(context, exchanges)
    }

    @Usage("List all bindings of the given exchange within the given virtual host where the exchange is the source")
    @Command
    def binds(@Usage("The exchagne name") @Option(names=["e", "exchange"]) String exchange, InvocationContext context) {
        List<Binding> bindings = getFacade(context).getExchangeSourceBindings(exchange)
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