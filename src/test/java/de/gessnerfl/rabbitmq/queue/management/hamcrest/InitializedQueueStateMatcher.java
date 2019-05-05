package de.gessnerfl.rabbitmq.queue.management.hamcrest;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class InitializedQueueStateMatcher extends BaseMatcher<String> {
    @Override
    public boolean matches(Object item) {
        return item == null || "running".equals(item);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("initialized queue should have 'null' state or 'running' state");
    }
}
