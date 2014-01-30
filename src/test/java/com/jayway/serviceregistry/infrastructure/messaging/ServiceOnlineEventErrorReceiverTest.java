package com.jayway.serviceregistry.infrastructure.messaging;

import com.jayway.serviceregistry.domain.ServiceRepository;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.LogLevel;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.Message;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.Messages;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ServiceOnlineEventErrorReceiverTest {
    // @formatter:off

    @Mock ServiceRepository serviceRepository;
    @Mock MessageSender messageSender;
    @InjectMocks ServiceMessageReceiver tested;

    @Test public void
    publishes_error_log_when_service_online_event_has_a_service_id_with_non_accepted_chars() {
        // Given
        Message message = Messages.serviceOnline("åäl odfs", "description", "creator" ,"serviceUrl", "sourceUrl");

        // When
        tested.handleMessage(message);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), argThat(errorLog()));
    }

    @Test public void
    publishes_error_log_when_service_online_event_body_doesnt_specify_description() {
        // Given
        Message message = Messages.serviceOnline("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        message.getBody().remove("description");

        // When
        tested.handleMessage(message);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), argThat(errorLog()));
    }
    
    @Test public void
    publishes_error_log_when_service_online_event_body_doesnt_specify_source_url() {
        // Given
        Message message = Messages.serviceOnline("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        message.getBody().remove("sourceUrl");

        // When
        tested.handleMessage(message);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), argThat(errorLog()));
    }

    @Test public void
    publishes_error_log_when_service_online_event_body_doesnt_specify_created_by() {
        // Given
        Message message = Messages.serviceOnline("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        message.getBody().remove("createdBy");

        // When
        tested.handleMessage(message);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), argThat(errorLog()));
    }

    @Test public void
    publishes_error_log_when_service_online_event_body_doesnt_specify_entry_point() {
        // Given
        Message message = Messages.serviceOnline("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        message.getBody().remove("serviceUrl");

        // When
        tested.handleMessage(message);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), argThat(errorLog()));
    }

    // @formatter:on

    private TypeSafeMatcher<Message> errorLog() {
        return new TypeSafeMatcher<Message>() {

            @SuppressWarnings("unchecked")
            @Override
            protected boolean matchesSafely(Message msg) {
                Map<String, Object> body = msg.getBody();
                return LogLevel.ERROR.equals(body.get("level"));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("was not error log message");
            }
        };
    }
}
