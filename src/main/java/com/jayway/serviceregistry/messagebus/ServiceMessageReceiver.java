package com.jayway.serviceregistry.messagebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ServiceMessageReceiver {
    private static final Logger log = LoggerFactory.getLogger(ServiceMessageReceiver.class);

    /**
     * Called by Spring in a magic way (from a {@link org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter}.
     */
    public void handleMessage(Map map) {
        log.info("Received {}", map);
    }
}
