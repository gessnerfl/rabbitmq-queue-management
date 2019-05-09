package de.gessnerfl.rabbitmq.queue.management.hamcrest;

import org.hamcrest.Matcher;

public class CustomMatchers {

    public static Matcher<String> matchesInitialQueueStateNullOrRunning(){
        return new InitializedQueueStateMatcher();
    }

    public static Matcher<String> matchesOperationId(){
        return new OperationIdMatcher();
    }
}
