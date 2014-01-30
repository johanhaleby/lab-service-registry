package com.jayway.serviceregistry.infrastructure.messaging;

import com.jayway.serviceregistry.domain.Service;
import com.jayway.serviceregistry.domain.ServiceRepository;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.LogLevel;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.Message;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.Messages;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.ServiceRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.jayway.serviceregistry.infrastructure.messaging.protocol.LogLevel.ERROR;
import static com.jayway.serviceregistry.infrastructure.messaging.protocol.LogLevel.INFO;
import static com.jayway.serviceregistry.infrastructure.messaging.protocol.MessageType.SERVICE_OFFLINE;
import static com.jayway.serviceregistry.infrastructure.messaging.protocol.MessageType.SERVICE_ONLINE;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ServiceMessageReceiver {
    private static final Pattern VALID_SERVICE_ID_PATTERN = Pattern.compile("[A-Za-z0-9_\\-]+");
    private static final Logger log = LoggerFactory.getLogger(ServiceMessageReceiver.class);
    private static final String UNKNOWN = "unknown";

    private static final String DESCRIPTION = "description";
    private static final String CREATED_BY = "createdBy";
    private static final String SERVICE_URL = "serviceUrl";
    private static final String SOURCE_URL = "sourceUrl";
    private static final String STREAM_ID = "streamId";
    private static final String MESSAGE_ID = "messageId";
    private static final String MESSAGE_TYPE = "type";
    private static final String CORRELATION = "correlation";
    private static final String APP_ID = "appId";
    private static final String TIMESTAMP = "timestamp";

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private MessageSender messageSender;

    /**
     * Called by Spring in a magic way (from a {@link org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter}.
     */
    public void handleMessage(Message message) {
        if (message == null) {
            return;
        }
        log.debug("Received {}", message);

        try {
            String messageType = message.meta(MESSAGE_TYPE);
            switch (messageType) {
                case SERVICE_ONLINE:
                    Service service = toService(message);
                    String messageId = message.meta(MESSAGE_ID);
                    addServiceToRegistry(messageId, service);
                    log(INFO, message, "Service " + service.getServiceId() + " registered.");
                    break;
                case SERVICE_OFFLINE:
                    String serviceId = removeServiceFromRegistry(message.<String>meta(STREAM_ID));
                    log(INFO, message, "Service " + serviceId + " unregistered.");
                    break;
                default:
                    log(ERROR, message, format("Message of type %s was received on %s topic.", messageType, Topic.SERVICE.getRoutingKey()));
                    break;
            }
        } catch (ServiceMessageNotCorrectException e) {
            log.debug("Failed to handle message because {}", StringUtils.uncapitalize(e.getMessage()));
        }
    }

    private String removeServiceFromRegistry(String serviceId) {
        serviceRepository.delete(serviceId); // Deleting service that doesn't exist returns silently
        return serviceId;
    }

    private void addServiceToRegistry(String messageId, Service service) {
        try {
            serviceRepository.save(service);
        } catch (DuplicateKeyException e) {
            log(LogLevel.ERROR, service.getServiceId(), messageId, format("A service was already registered with name %s.", service.getDescription()));
        }
    }

    @SuppressWarnings("unchecked")
    private Service toService(Message message) {
        String streamId = message.meta("streamId");
        if (!VALID_SERVICE_ID_PATTERN.matcher(streamId).matches()) {
            String errorMessage = format("The serviceId of event %s with streamId %s is must match reg exp %s.", SERVICE_ONLINE, streamId, VALID_SERVICE_ID_PATTERN.toString());
            log(LogLevel.ERROR, message, errorMessage);
            throw new ServiceMessageNotCorrectException(errorMessage);
        }

        String description = getStringValueInBodyOrLogError(message, DESCRIPTION, format("The body of event %s with streamId %s is missing attribute '%s'.", SERVICE_ONLINE, streamId, DESCRIPTION));
        String createdBy = getStringValueInBodyOrLogError(message, CREATED_BY, format("The body of event %s with streamId %s is missing attribute '%s'.", SERVICE_ONLINE, streamId, CREATED_BY));
        String serviceUrl = getStringValueInBodyOrLogError(message, SERVICE_URL, format("The body of event %s with streamId %s is missing attribute '%s'", SERVICE_ONLINE, streamId, SERVICE_URL));
        String sourceUrl = getStringValueInBodyOrLogError(message, SOURCE_URL, format("The body of event %s with streamId %s is missing attribute '%s'", SERVICE_ONLINE, streamId, SOURCE_URL));

        Map<String, Object> supplementaryBodyAttributes = new HashMap<>(message.getBody());
        supplementaryBodyAttributes.keySet().removeAll(asList(DESCRIPTION, CREATED_BY, SERVICE_URL, SOURCE_URL));

        Map<String, Object> supplementaryMetaAttributes = new HashMap<>(message.getMeta());
        supplementaryMetaAttributes.keySet().removeAll(asList(STREAM_ID, APP_ID, TIMESTAMP, MESSAGE_ID, MESSAGE_TYPE));

        return new Service(streamId, description, createdBy, serviceUrl, sourceUrl, supplementaryBodyAttributes, supplementaryMetaAttributes);
    }

    private void log(LogLevel logLevel, Message message, String logMessage) {
        String streamId = message.meta(STREAM_ID);
        String messageId = message.meta(MESSAGE_ID);
        log(logLevel, streamId, messageId, logMessage);
    }

    private void log(LogLevel logLevel, String streamId, String messageId, String message) {
        Map<String, Object> meta = new HashMap<>();
        Map<String, Object> correlation = new HashMap<>();
        meta.put(CORRELATION, correlation);
        correlation.put(STREAM_ID, streamId == null ? UNKNOWN : streamId);
        correlation.put(MESSAGE_ID, messageId == null ? UNKNOWN : messageId);
        messageSender.sendMessage(Topic.LOG, Messages.log(logLevel, ServiceRegistry.APP_ID, message, meta));
        log.debug("Sent log message '{}' with level {}.", message, logLevel);
        if (logLevel == ERROR) {
            throw new ServiceMessageNotCorrectException(message);
        }
    }

    private String getStringValueInBodyOrLogError(Message message, String key, String errorMessage) {
        String string = toString(message.getBody(), key);
        if (isBlank(string)) {
            log(LogLevel.ERROR, message, errorMessage);
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
            log(LogLevel.ERROR, null, null, "The attribute " + key + " must be a String, was " + classOf(value) + ".");
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
