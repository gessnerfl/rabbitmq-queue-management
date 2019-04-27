package de.gessnerfl.rabbitmq.queue.management.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class IndexControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Test
    public void shouldReturnListOfQueues() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("queues", hasSize(1)))
            .andExpect(view().name("index"));
    }
    
}
