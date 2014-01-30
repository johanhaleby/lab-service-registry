package com.jayway.serviceregistry.infrastructure.messaging;

import com.jayway.serviceregistry.infrastructure.messaging.protocol.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.serviceregistry.infrastructure.messaging.protocol.MessageType.*;

@Service
public class MessageSender {

    private static final String FALLBACK_ROUTING_KEY = "log";

    private static final Map<String, String> EVENT_TYPE_TO_ROUTING_KEY = new HashMap<String, String>() {{
        put(GAME_CREATED, Topic.GAME.getRoutingKey());
        put(GAME_ENDED, Topic.GAME.getRoutingKey());
        put(LOG, Topic.LOG.getRoutingKey());
        put(SERVICE_ONLINE, Topic.SERVICE.getRoutingKey());
        put(SERVICE_OFFLINE, Topic.SERVICE.getRoutingKey());
    }};

    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendMessage(Topic topic, Message message) {
        sendMessage(topic, findRoutingKeyOrElse(message, FALLBACK_ROUTING_KEY), message);
    }

    public void sendMessage(Topic topic, String routingKey, Message message) {
        Assert.notNull(topic, "Topic cannot be null");
        Assert.notNull(routingKey, "Routing key cannot be null");
        Assert.notNull(message, "Message cannot be null");
        amqpTemplate.convertAndSend(Topic.getLabExchange(), routingKey, message);
    }

    String findRoutingKeyOrElse(Message message, String fallbackRoutingKey) {
        Assert.notNull(message, "Message cannot be null");
        Object type = message.meta("type");
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
