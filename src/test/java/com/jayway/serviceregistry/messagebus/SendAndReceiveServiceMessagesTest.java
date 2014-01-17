package com.jayway.serviceregistry.messagebus;

import com.jayway.serviceregistry.boot.ServiceRegistryStart;
import com.jayway.serviceregistry.domain.Service;
import com.jayway.serviceregistry.domain.ServiceRepository;
import com.jayway.serviceregistry.messagebus.protocol.Messages;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.UUID;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.to;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceRegistryStart.class)
public class SendAndReceiveServiceMessagesTest {

    @Autowired
    MessageSender messageSender;

    @Autowired
    ServiceRepository serviceRepository;

    @Before @After
    public void
    drop_mongo_service_collection() throws Exception {
       serviceRepository.deleteAll();
    }

    @Test public void
    service_online_event_causes_a_new_service_to_be_registered_in_the_service_repository() throws Exception {
        // Given
        String serviceId = UUID.randomUUID().toString();
        Map<String,Object> message = Messages.serviceOnlineEvent(serviceId, "service1", "http://someurl1.com", "Johan");

        // When
        messageSender.sendMessage(Topic.SERVICE, message);

        // Then
        await().atMost(5, SECONDS).untilCall(to(serviceRepository).count(), is(1L));
        Service service = serviceRepository.findByName("service1");

        assertThat(service.getName()).isEqualTo("service1");
        assertThat(service.getEntryPoint()).isEqualTo("http://someurl1.com");
        assertThat(service.getServiceId()).isEqualTo(serviceId);
        assertThat(service.getCreatedBy()).isEqualTo("Johan");
    }
}
