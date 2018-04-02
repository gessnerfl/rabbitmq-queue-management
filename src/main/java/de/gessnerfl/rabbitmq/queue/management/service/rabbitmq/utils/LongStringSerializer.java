package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rabbitmq.client.LongString;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class LongStringSerializer extends JsonSerializer<LongString> {
    @Override
    public void serialize(LongString value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.toString());
    }

}
