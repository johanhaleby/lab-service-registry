package com.jayway.serviceregistry.messagebus;

import com.jayway.serviceregistry.domain.Service;
import com.jayway.serviceregistry.domain.ServiceRepository;
import com.jayway.serviceregistry.messagebus.protocol.LogLevel;
import com.jayway.serviceregistry.messagebus.protocol.Messages;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.serviceregistry.messagebus.protocol.EventType.SERVICE_OFFLINE_EVENT;
import static com.jayway.serviceregistry.messagebus.protocol.EventType.SERVICE_ONLINE_EVENT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ServiceMessageReceiver {
    private static final Logger log = LoggerFactory.getLogger(ServiceMessageReceiver.class);
    private static final String UNKNOWN = "unknown";
    private static final String SERVICE_REGISTRY = "service-registry";

    private static final String DESCRIPTION = "description";
    private static final String CREATED_BY = "createdBy";
    private static final String SERVICE_URL = "serviceUrl";
    private static final String SOURCE_URL = "sourceUrl";
    private static final String STREAM_ID = "streamId";
    private static final String MESSAGE_ID = "messageId";

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private MessageSender messageSender;

    /**
     * Called by Spring in a magic way (from a {@link org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter}.
     */
    public void handleMessage(Object object) {
        log.debug("Received {}", object);

        if (!(object instanceof Map)) {
            log.debug("Received message couldn't be deserialized as a Map");
            return;
        }

        Map map = (Map) object;

        try {
            String eventType = getStringOrLogError(map, "type", "No message type was defined");

            switch (eventType) {
                case SERVICE_ONLINE_EVENT:
                    Service service = toService(map);
                    String messageId = getStringOrLogError(map, MESSAGE_ID, format("%s with streamId %s is missing attribute '%s'.", SERVICE_ONLINE_EVENT, service.getServiceId(), MESSAGE_ID));
                    addServiceToRegistry(messageId, service);
                    break;
                case SERVICE_OFFLINE_EVENT:
                    removeServiceFromRegistry(map);
                    break;
                default:
            }
        } catch (ServiceMessageNotCorrectException e) {
            log.debug("Failed to handle message because {}", StringUtils.uncapitalize(e.getMessage()));
        }
    }

    private void removeServiceFromRegistry(Map map) {
        String serviceId = getStringOrLogError(map, STREAM_ID, "Message didn't define the id of service to remove (streamId attribute is missing)");
        serviceRepository.delete(serviceId); // Deleting service that doesn't exist returns silently
    }

    private void addServiceToRegistry(String messageId, Service service) {
        try {
            serviceRepository.save(service);
        } catch (DuplicateKeyException e) {
            logError(service.getServiceId(), messageId, format("A service was already registered with name %s.", service.getDescription()));
        }
    }

    @SuppressWarnings("unchecked")
    private Service toService(Map map) {
        String streamId = getStringOrLogError(map, STREAM_ID, SERVICE_ONLINE_EVENT + " is missing attribute streamId.");

        Object potentialBody = map.get("body");
        if (potentialBody == null) {
            logError(map, "Body was undefined in " + SERVICE_ONLINE_EVENT + " with streamId " + streamId);
        }

        if (!(potentialBody instanceof Map)) {
            logError(map, "Body was not defined correctly in " + SERVICE_ONLINE_EVENT + " with streamId " + streamId);
        }

        Map<String, Object> body = ((Map<String, Object>) potentialBody);
        String description = getStringOrLogError(body, DESCRIPTION, format("The body of event %s with streamId %s is missing attribute '%s'.", SERVICE_ONLINE_EVENT, streamId, DESCRIPTION));
        String createdBy = getStringOrLogError(body, CREATED_BY, format("The body of event %s with streamId %s is missing attribute '%s'.", SERVICE_ONLINE_EVENT, streamId, CREATED_BY));
        String serviceUrl = getStringOrLogError(body, SERVICE_URL, format("The body of event %s with streamId %s is missing attribute '%s'", SERVICE_ONLINE_EVENT, streamId, SERVICE_URL));
        String sourceUrl = getStringOrLogError(body, SOURCE_URL, format("The body of event %s with streamId %s is missing attribute '%s'", SERVICE_ONLINE_EVENT, streamId, SOURCE_URL));

        Map<String, Object> supplementaryBodyAttributes = new HashMap<>(body);
        supplementaryBodyAttributes.keySet().removeAll(asList(STREAM_ID, DESCRIPTION, CREATED_BY, SERVICE_URL, SOURCE_URL));

        Object potentialMeta = getOrDefault(map, "meta", new HashMap<String, Object>());
        if (!(potentialMeta instanceof Map)) {
            logError(map, format("The meta part of event %s with streamId %s is not defined correctly. Was %s but required is JSON object (Map).", SERVICE_ONLINE_EVENT, streamId, potentialMeta == null ? null : potentialMeta.getClass().getSimpleName()));
        }
        Map<String, Object> meta = (Map<String, Object>) potentialMeta;

        return new Service(streamId, description, createdBy, serviceUrl, sourceUrl, supplementaryBodyAttributes, meta);
    }

    // This method will be available in the Map interface in Java 8.
    private Object getOrDefault(Map map, String meta, HashMap<String, Object> defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        Object value = map.get(meta);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private void logError(Map map, String message) {
        String streamId = toString(map, STREAM_ID);
        String messageId = toString(map, MESSAGE_ID);
        logError(streamId, messageId, message);
    }

    private void logError(String streamId, String messageId, String message) {
        Map<String, Object> meta = new HashMap<>();
        meta.put(STREAM_ID, streamId == null ? UNKNOWN : streamId);
        meta.put(MESSAGE_ID, messageId == null ? UNKNOWN : messageId);
        messageSender.sendMessage(Topic.LOG, Messages.logEvent(LogLevel.ERROR, SERVICE_REGISTRY, message, meta));
        throw new ServiceMessageNotCorrectException(message);
    }

    private String getStringOrLogError(Map map, String key, String errorMessage) {
        String string = toString(map, key);
        if (isBlank(string)) {
            logError(map, errorMessage);
        }
        return string;
    }

    private String toString(Map map, String key) {
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        if (value == null) {
            return null;
        } else if (!(value instanceof String)) {
            logError(null, null, "The attribute " + key + " must be a String, was " + classOf(value) + ".");
        }
        return (String) value;
    }

    private String classOf(Object value) {
        if (value == null) {
            return "null";
        }
        return value.getClass().getSimpleName();
    }

    static class ServiceMessageNotCorrectException extends RuntimeException {
        public ServiceMessageNotCorrectException(String message) {
            super(message);
        }
    }
}
