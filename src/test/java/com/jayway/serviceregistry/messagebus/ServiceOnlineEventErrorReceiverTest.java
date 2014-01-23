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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ServiceOnlineEventErrorReceiverTest {
    // @formatter:off

    @Mock ServiceRepository serviceRepository;
    @Mock MessageSender messageSender;
    @InjectMocks ServiceMessageReceiver tested;

    @Test public void
    publishes_error_log_when_service_online_event_message_body_is_not_defined() {
        // Given
        Map<String,Object> event = Messages.serviceOnlineEvent("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        event.remove("body");

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }

    @Test public void
    publishes_error_log_when_service_online_event_message_body_is_not_defined_correctly() {
        // Given
        Map<String,Object> event = Messages.serviceOnlineEvent("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        event.put("body", "something");

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }

    @Test public void
    publishes_error_log_when_service_online_event_doesnt_specify_stream_id() {
        // Given
        Map<String,Object> event = Messages.serviceOnlineEvent("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        event.remove("streamId");

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }

    @Test public void
    publishes_error_log_when_service_online_event_specifies_stream_id_as_int() {
        // Given
        Map<String,Object> event = Messages.serviceOnlineEvent("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        event.put("streamId", 2);

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }

    @Test public void
    publishes_error_log_when_service_online_event_body_doesnt_specify_description() {
        // Given
        Map<String,Object> event = Messages.serviceOnlineEvent("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        ((Map) event.get("body")).remove("description");

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }
    
    @Test public void
    publishes_error_log_when_service_online_event_body_doesnt_specify_source_url() {
        // Given
        Map<String,Object> event = Messages.serviceOnlineEvent("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        ((Map) event.get("body")).remove("sourceUrl");

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }

    @Test public void
    publishes_error_log_when_service_online_event_body_doesnt_specify_created_by() {
        // Given
        Map<String,Object> event = Messages.serviceOnlineEvent("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        ((Map) event.get("body")).remove("createdBy");

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }

    @Test public void
    publishes_error_log_when_service_online_event_body_doesnt_specify_entry_point() {
        // Given
        Map<String,Object> event = Messages.serviceOnlineEvent("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        ((Map) event.get("body")).remove("serviceUrl");

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }

    @Test public void
    publishes_error_log_when_service_online_event_meta_is_not_of_a_json_object() {
        // Given
        Map<String,Object> event = Messages.serviceOnlineEvent("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        event.put("meta", 2);

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender).sendMessage(eq(Topic.LOG), anyMap());
    }

    @Test public void
    doesnt_publish_error_log_when_service_online_event_meta_is_missing() {
        // Given
        Map<String,Object> event = Messages.serviceOnlineEvent("id", "description", "creator" ,"serviceUrl", "sourceUrl");
        event.remove("meta");

        // When
        tested.handleMessage(event);

        // Then
        verify(messageSender, never()).sendMessage(eq(Topic.LOG), anyMap());
    }
    // @formatter:on
}
