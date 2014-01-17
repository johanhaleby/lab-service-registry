package com.jayway.serviceregistry.messagebus;

import com.jayway.serviceregistry.domain.Service;
import com.jayway.serviceregistry.domain.ServiceRepository;
import com.jayway.serviceregistry.messagebus.protocol.LogLevel;
import com.jayway.serviceregistry.messagebus.protocol.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.jayway.serviceregistry.messagebus.protocol.EventType.SERVICE_OFFLINE_EVENT;
import static com.jayway.serviceregistry.messagebus.protocol.EventType.SERVICE_ONLINE_EVENT;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ServiceMessageReceiver {
    private static final Logger log = LoggerFactory.getLogger(ServiceMessageReceiver.class);

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private MessageSender messageSender;

    /**
     * Called by Spring in a magic way (from a {@link org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter}.
     */
    public void handleMessage(Map map) {
        log.debug("Received {}", map);

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
    }

    private void removeServiceFromRegistry(Map map) {
        String serviceName = getStringOrLogError(map, "streamId", "Message didn't define name of service to remove");
        Service service = serviceRepository.findByName(serviceName);
        if (service != null) {
            serviceRepository.delete(service);
        }
    }

    private void addServiceToRegistry(Service service) {
        try {
            serviceRepository.save(service);
        } catch (DuplicateKeyException e) {
            // Allows for idempotency
            log.debug("A service was already registered with name {}", service.getName());
        }
    }

    private Service toService(Map map) {
        Object body = map.get("body");
        if (body == null) {
            logError(map, "Body was undefined in " + SERVICE_ONLINE_EVENT);
        }

        if (!(body instanceof Map)) {
            logError(map, "Body was not defined correctly in " + SERVICE_ONLINE_EVENT);
        }

        Map serviceOnlineEvent = ((Map) body);
        String serviceId = getStringOrLogError(map, "streamId", SERVICE_ONLINE_EVENT + " is missing attribute streamId.");
        String name = getStringOrLogError(serviceOnlineEvent, "name", SERVICE_ONLINE_EVENT + " body is missing attribute name.");
        String createdBy = getStringOrLogError(serviceOnlineEvent, "createdBy", SERVICE_ONLINE_EVENT + " body is missing attribute createdBy.");
        String entryPoint = getStringOrLogError(serviceOnlineEvent, "entryPoint", SERVICE_ONLINE_EVENT + " body is missing attribute entryPoint.");
        return new Service(serviceId, name, createdBy, entryPoint);
    }

    private void logError(Map map, String message) {
        String streamId = toString(map, "streamId");
        messageSender.sendMessage(Topic.LOG, Messages.logEvent("service-registry", LogLevel.ERROR, streamId == null ? "" : streamId, message));
    }

    private String getStringOrLogError(Map map, String key, String errorMessage) {
        String eventType = toString(map, key);
        if (isBlank(eventType)) {
            log.debug(errorMessage);
            logError(map, errorMessage);
        }
        return eventType;
    }

    private String toString(Map map, String key) {
        return (String) map.get(key);
    }
}
