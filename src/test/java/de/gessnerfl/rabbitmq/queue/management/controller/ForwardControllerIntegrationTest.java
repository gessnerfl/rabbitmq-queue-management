package de.gessnerfl.rabbitmq.queue.management.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;

public class ForwardControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Test
    public void shouldReturnListOfQueues() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("index.html"));
    }
    
}
