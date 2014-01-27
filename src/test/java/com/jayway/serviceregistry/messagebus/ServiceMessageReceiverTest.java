package com.jayway.serviceregistry.messagebus;

import com.jayway.serviceregistry.domain.ServiceRepository;
import com.jayway.serviceregistry.messagebus.protocol.Messages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ServiceMessageReceiverTest {
    // @formatter:off

    @Mock ServiceRepository serviceRepository;
    @Mock MessageSender messageSender;
    @InjectMocks ServiceMessageReceiver tested;

    @Test public void
    returns_silently_when_message_is_not_instance_of_map() {
        // When
        tested.handleMessage(new Object());
    }

    @Test public void
    publishes_error_log_when_no_message_type_is_defined() {
        // Given
        Map<String,Object> event = Messages.gameCreatedEvent("game1", "Ikk", "url", Collections.<String>emptyList());
        event.remove("type");

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }
    // @formatter:on
}
