package com.jayway.serviceregistry.messagebus;

import com.jayway.serviceregistry.domain.ServiceRepository;
import com.jayway.serviceregistry.messagebus.protocol.Messages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ServiceOfflineEventErrorReceiverTest {
    // @formatter:off

    @Mock ServiceRepository serviceRepository;
    @Mock MessageSender messageSender;
    @InjectMocks ServiceMessageReceiver tested;

    @Test public void
    publishes_error_log_when_service_online_event_is_missing_a_stream_id() {
        // Given
        Map<String,Object> event = Messages.serviceOfflineEvent("id");
        event.remove("streamId");

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }

    @Test public void
    publishes_error_log_when_service_online_event_is_using_a_stream_id_that_is_not_a_string() {
        // Given
        Map<String,Object> event = Messages.serviceOfflineEvent("1");
        event.put("streamId", 2);

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }

    @Test public void
    publishes_error_log_when_service_online_event_is_using_a_stream_id_that_is_null() {
        // Given
        Map<String,Object> event = Messages.serviceOfflineEvent("1");
        event.put("streamId", null);

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }


    // @formatter:on
}
