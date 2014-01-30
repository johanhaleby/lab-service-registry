package com.jayway.serviceregistry.messagebus;

import com.jayway.serviceregistry.messagebus.protocol.LogLevel;
import com.jayway.serviceregistry.messagebus.protocol.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
class ServiceRegistryStoppedLogger implements ApplicationListener<ContextClosedEvent> {

    @Autowired
    MessageSender messageSender;

    public void onApplicationEvent(ContextClosedEvent event) {
        messageSender.sendMessage(Topic.LOG, Messages.logEvent(LogLevel.INFO, "Service Registry started"));
    }
}
