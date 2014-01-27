package com.jayway.serviceregistry.messagebus;

import com.jayway.serviceregistry.messagebus.protocol.LogLevel;
import com.jayway.serviceregistry.messagebus.protocol.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

@Component
class ServiceRegistryStartedLogger implements ApplicationListener<ContextStartedEvent> {

    @Autowired
    MessageSender messageSender;

    public void onApplicationEvent(ContextStartedEvent event) {
        messageSender.sendMessage(Topic.LOG, Messages.logEvent(LogLevel.INFO, "service-registry-started", "Service Registry started"));
    }
}
