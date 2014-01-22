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
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ServiceMessageReceiver {
    private static final Logger log = LoggerFactory.getLogger(ServiceMessageReceiver.class);
    private static final String EMPTY_STRING = "";

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
                    addServiceToRegistry(service);
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
        String serviceId = getStringOrLogError(map, "streamId", "Message didn't define the id of service to remove (streamId attribute is missing)");
        serviceRepository.delete(serviceId); // Deleting service that doesn't exist returns silently
    }

    private void addServiceToRegistry(Service service) {
        try {
            serviceRepository.save(service);
        } catch (DuplicateKeyException e) {
            // Allows for idempotency
            log.debug("A service was already registered with name {}", service.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private Service toService(Map map) {
        String streamId = getStringOrLogError(map, "streamId", SERVICE_ONLINE_EVENT + " is missing attribute streamId.");

        Object body = map.get("body");
        if (body == null) {
            logError(map, "Body was undefined in " + SERVICE_ONLINE_EVENT + " with streamId " + streamId);
        }

        if (!(body instanceof Map)) {
            logError(map, "Body was not defined correctly in " + SERVICE_ONLINE_EVENT + " with streamId " + streamId);
        }

        Map serviceOnlineEvent = ((Map) body);
        String name = getStringOrLogError(serviceOnlineEvent, "name", format("The body of event %s with streamId %s is missing attribute 'name'.", SERVICE_ONLINE_EVENT, streamId));
        String createdBy = getStringOrLogError(serviceOnlineEvent, "createdBy", format("The body of event %s with streamId %s is missing attribute 'createdBy'.", SERVICE_ONLINE_EVENT, streamId));
        String entryPoint = getStringOrLogError(serviceOnlineEvent, "entryPoint", format("The body of event %s with streamId %s is missing attribute 'entryPoint'", SERVICE_ONLINE_EVENT, streamId));

        Object meta = map.getOrDefault("meta", new HashMap<>());
        if (!(meta instanceof Map)) {
            logError(map, format("The meta part of event %s with streamId %s is not defined correctly. Was %s but required is JSON object (Map).", SERVICE_ONLINE_EVENT, streamId, meta == null ? null : meta.getClass().getSimpleName()));
        }

        Service service = new Service(streamId, name, createdBy, entryPoint);
        service.setMeta((Map<String, Object>) meta);
        return service;
    }

    private void logError(Map map, String message) {
        String streamId = toString(map, "streamId");
        logError(streamId, message);
    }

    private void logError(String streamId, String message) {
        messageSender.sendMessage(Topic.LOG, Messages.logEvent(LogLevel.ERROR, streamId == null ? EMPTY_STRING : streamId, message));
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
            logError((String) null, "The attribute " + key + " must be a String, was " + classOf(value) + ".");
        }
        return (String) value;
    }

    private String classOf(Object value) {
        if (value == null) {
            return "null";
        }
        return value.getClass().getSimpleName();
    }

    public static class ServiceMessageNotCorrectException extends RuntimeException {
        public ServiceMessageNotCorrectException(String message) {
            super(message);
        }
    }


}
