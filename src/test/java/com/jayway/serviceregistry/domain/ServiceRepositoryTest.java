package com.jayway.serviceregistry.domain;

import com.jayway.serviceregistry.boot.ServiceRegistryStart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceRegistryStart.class)
public class ServiceRepositoryTest {
    @Autowired
    ServiceRepository serviceRepository;

    @Before @After public void
    drop_mongo_service_collection() throws Exception {
        serviceRepository.deleteAll();
    }

    @Test public void
    service_repository_can_find_services_by_name() {
        // Given
        Service savedService = serviceRepository.save(new Service("my-service", "Johan", "http://www.google.com"));

        // When
        Service foundService = serviceRepository.findByName("my-service");

        // Then
        assertThat(foundService).isEqualTo(savedService);
    }

    @Test public void
    service_repository_can_find_services_by_creator() {
        // Given
        Service savedService1 = serviceRepository.save(new Service("my-service1", "Johan", "http://www.google.com"));
        Service savedService2 = serviceRepository.save(new Service("my-service2", "Johan", "http://www.google.com/search?q=my-service2"));
        serviceRepository.save(new Service("my-service3", "Someone Else", "http://www.google.com/search?q=my-service3"));

        // When
        List<Service> foundServices = serviceRepository.findByCreatedBy("Johan");

        // Then
        assertThat(foundServices).containsOnlyOnce(savedService1, savedService2);
    }
}
