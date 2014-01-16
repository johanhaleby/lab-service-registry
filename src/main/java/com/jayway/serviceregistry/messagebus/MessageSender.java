package com.jayway.serviceregistry.messagebus;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class MessageSender {

    private static final String ANONYMOUS_ROUTING_KEY = "anonymous.info";

    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendMessage(Topic topic, Object object) {
        Assert.notNull(topic, "Topic cannot be null");
        Assert.notNull(object, "Message cannot be null");
        amqpTemplate.convertAndSend(topic.getExchange(), ANONYMOUS_ROUTING_KEY, object);
    }
}
