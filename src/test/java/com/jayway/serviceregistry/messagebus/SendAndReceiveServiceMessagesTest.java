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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.to;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

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
    service_online_event_without_meta_causes_a_new_service_to_be_registered_in_the_service_repository() throws Exception {
        // Given
        String serviceId = UUID.randomUUID().toString();
        Map<String,Object> message = Messages.serviceOnlineEvent(serviceId, "service1", "Johan", "http://someurl1.com", "http://source1.com");

        // When
        messageSender.sendMessage(Topic.SERVICE, message);

        // Then
        Service service = await().atMost(5, SECONDS).untilCall(to(serviceRepository).findOne(serviceId), notNullValue());

        assertThat(service.getDescription()).isEqualTo("service1");
        assertThat(service.getServiceUrl()).isEqualTo("http://someurl1.com");
        assertThat(service.getSourceUrl()).isEqualTo("http://source1.com");
        assertThat(service.getServiceId()).isEqualTo(serviceId);
        assertThat(service.getCreatedBy()).isEqualTo("Johan");
        assertThat(service.getOptionalProperties()).isEmpty();
    }

    @Test public void
    service_online_event_with_meta_causes_a_new_service_to_be_registered_in_the_service_repository() throws Exception {
        // Given
        String serviceId = UUID.randomUUID().toString();
        Map<String, Object> meta = new HashMap<>();
        meta.put("type", "nice-service");
        meta.put("ttl", "42 hours");
        Map<String,Object> message = Messages.serviceOnlineEvent(serviceId, "service1", "Johan", "http://someurl1.com", "http://source.com", meta);

        // When
        messageSender.sendMessage(Topic.SERVICE, message);

        // Then
        Service service = await().atMost(5, SECONDS).untilCall(to(serviceRepository).findOne(serviceId), notNullValue());

        assertThat(service.getDescription()).isEqualTo("service1");
        assertThat(service.getServiceUrl()).isEqualTo("http://someurl1.com");
        assertThat(service.getServiceId()).isEqualTo(serviceId);
        assertThat(service.getCreatedBy()).isEqualTo("Johan");
        assertThat(service.getMeta()).containsEntry("type", "nice-service").containsEntry("ttl", "42 hours").hasSize(2);
        assertThat(service.getOptionalProperties()).isEmpty();
    }

    @Test public void
    service_offline_event_causes_the_service_to_be_unregistered_from_the_service_repository() throws Exception {
        // Given
        Service service1 = new Service("service1", "My Service 1", "Johan", "http://someurl1.com", "http://source1.com");
        Service service2 = new Service("service2", "My Service 2", "Johan", "http://someurl2.com", "http://source1.com");
        serviceRepository.save(service1);
        serviceRepository.save(service2);

        Map<String,Object> message = Messages.serviceOfflineEvent(service1.getServiceId());

        // When
        messageSender.sendMessage(Topic.SERVICE, message);

        // Then
        await().atMost(5, SECONDS).untilCall(to(serviceRepository).findOne("service1"), nullValue());
        assertThat(serviceRepository.findOne("service2")).isNotNull();
    }
}
