package de.gessnerfl.rabbitmq.queue.management.service.console;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.crsh.command.InvocationContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.json.JsonSerializer;

@RunWith(MockitoJUnitRunner.class)
public class ConsoleUtilTest {
    
    @Mock
    private JsonSerializer serializer;
    
    @InjectMocks
    private ConsoleUtil sut;
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void shouldRenderEntriesAsJsonAndProvideJsonToContext() throws Exception{
        String json = "json";
        Message message = mock(Message.class);
        List<Message> messages = Arrays.asList(message, message);
        when(serializer.toJson(message)).thenReturn(json);
        
        InvocationContext context = mock(InvocationContext.class);
        
        sut.render(context, messages);
        
        verify(context).provide("json\n");
        verify(context).provide("json");
    }

}
