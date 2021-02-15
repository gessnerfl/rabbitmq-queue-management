package de.gessnerfl.rabbitmq.queue.management.service.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

public class JsonSerializerTest {
    private final static String JSON = "{\"myString\":\"test\",\"myNumber\":123}";

    private JsonSerializer sut;

    @BeforeEach
    public void init() {
        sut = new JsonSerializer(new Gson());
    }

    @Test
    public void shouldSerializeToJson() {
        Dummy dummy = new Dummy("test", 123);

        String result = sut.toJson(dummy);

        assertEquals(JSON, result);
    }

    @Test
    public void shouldDeserializeFromJson() {
        Dummy result = sut.fromJson(Dummy.class, JSON);

        assertEquals("test", result.myString);
        assertEquals(123, result.myNumber);
    }

    private class Dummy {
        final String myString;
        final int myNumber;

        public Dummy(String myString, int myNumber) {
            this.myString = myString;
            this.myNumber = myNumber;
        }
    }
}
