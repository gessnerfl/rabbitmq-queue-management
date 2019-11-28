package de.gessnerfl.rabbitmq.queue.management.controller;


import de.gessnerfl.rabbitmq.queue.management.AbstractIntegrationTestWithRabbitMqContainer;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public abstract class AbstractControllerIntegrationTest extends AbstractIntegrationTestWithRabbitMqContainer {
    protected MediaType contentType = MediaType.APPLICATION_JSON;
    protected MockMvc mockMvc;
    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Before
    public void initMockMvc(){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
}
