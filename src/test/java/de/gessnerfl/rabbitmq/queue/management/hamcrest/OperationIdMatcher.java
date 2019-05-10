package de.gessnerfl.rabbitmq.queue.management.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class OperationIdMatcher extends TypeSafeMatcher<String> {

    @Override
    protected boolean matchesSafely(String item) {
        return item.matches("[A-Ha-h0-9]+");
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("matches when the given string is a proper operation id");
    }
}
