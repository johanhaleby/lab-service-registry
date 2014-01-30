package com.jayway.serviceregistry.infrastructure.messaging;

import com.jayway.serviceregistry.infrastructure.messaging.protocol.LogLevel;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.Messages;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

@Component
class ServiceRegistryStartedLogger implements ApplicationListener<ContextStartedEvent> {

    @Autowired
    MessageSender messageSender;

    public void onApplicationEvent(ContextStartedEvent event) {
        messageSender.sendMessage(Topic.LOG, Messages.log(LogLevel.INFO, ServiceRegistry.APP_ID, "Service Registry started"));
    }
}
