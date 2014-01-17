package com.jayway.serviceregistry.messagebus;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.serviceregistry.messagebus.protocol.EventType.*;

@Service
public class MessageSender {

    private static final String ANONYMOUS_ROUTING_KEY = "lab.log";

    private static final Map<String, String> EVENT_TYPE_TO_ROUTING_KEY = new HashMap<String, String>() {{
        put(GAME_CREATED_EVENT, "lab.game");
        put(GAME_ENDED_EVENT, "lab.game");
        put(LOG_EVENT, "lab.log");
        put(SERVICE_ONLINE_EVENT, "lab.service");
        put(SERVICE_OFFLINE_EVENT, "lab.service");
    }};

    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendMessage(Topic topic, Map<String, Object> message) {
        sendMessage(topic, findRoutingKeyOrElse(message, ANONYMOUS_ROUTING_KEY), message);
    }

    public void sendMessage(Topic topic, String routingKey, Map<String, Object> message) {
        Assert.notNull(topic, "Topic cannot be null");
        Assert.notNull(routingKey, "Routing key cannot be null");
        Assert.notNull(message, "Message cannot be null");
        amqpTemplate.convertAndSend(Topic.getLabExchange(), routingKey, message);
    }

    String findRoutingKeyOrElse(Map<String, Object> message, String fallbackRoutingKey) {
        Assert.notNull(message, "Message cannot be null");
        Object type = message.get("type");
        String routingKey = fallbackRoutingKey;
        if (type instanceof String) {
            String messageType = (String) type;
            if (!StringUtils.isBlank(messageType)) {
                routingKey = getOrElse(EVENT_TYPE_TO_ROUTING_KEY.get(messageType), fallbackRoutingKey);
            }
        }
        return routingKey;
    }

    private String getOrElse(String routingKey, String fallback) {
        return routingKey == null ? fallback : routingKey;
    }
}
