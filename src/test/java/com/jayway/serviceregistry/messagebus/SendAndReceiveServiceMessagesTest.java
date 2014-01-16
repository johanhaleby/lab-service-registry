package com.jayway.serviceregistry.messagebus;

import com.jayway.serviceregistry.boot.ServiceRegistryStart;
import com.jayway.serviceregistry.messagebus.protocol.Messages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceRegistryStart.class)
public class SendAndReceiveServiceMessagesTest {

    @Autowired
    MessageSender messageSender;

    @Test public void
    _() throws Exception {
        // Given
        // When
        messageSender.sendMessage(Topic.SERVICE, Messages.serviceOnlineEvent(UUID.randomUUID().toString(), "service1", "http://someurl1.com", "Johan"));
        // Then
        Thread.sleep(2000);
    }
}
