package de.gessnerfl.rabbitmq.queue.management.service.console;

import java.util.List;

import org.crsh.command.InvocationContext;
import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.queue.management.service.json.JsonSerializer;

@Service
public class ConsoleUtil {

    private final JsonSerializer serializer;

    public ConsoleUtil(JsonSerializer serializer) {
        super();
        this.serializer = serializer;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> void render(InvocationContext context, List<T> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            String json = serializer.toJson(list.get(i));
            String linebreak = i < (list.size() - 1) ? "\n" : "";
            context.provide(json + linebreak);
        }
    }

}
