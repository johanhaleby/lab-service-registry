package com.jayway.serviceregistry.domain;

import com.jayway.serviceregistry.boot.ServiceRegistryStart;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceRegistryStart.class)
public class ServiceRepositoryTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    ServiceRepository serviceRepository;

    @Before @After public void
    drop_mongo_service_collection() throws Exception {
        serviceRepository.deleteAll();
    }

    @Test public void
    finds_services_by_name() {
        // Given
        Service savedService = serviceRepository.save(new Service("my-service", "Description", "Johan", "http://www.google.com", "<hidden>"));

        // When
        Service foundService = serviceRepository.findOne("my-service");

        // Then
        assertThat(foundService).isEqualTo(savedService);
    }

    @Test public void
    finds_services_by_creator() {
        // Given
        Service savedService1 = serviceRepository.save(new Service("my-service1", "Description 1", "Johan", "http://www.google.com", "http://someurl.com"));
        Service savedService2 = serviceRepository.save(new Service("my-service2", "Description 2", "Johan", "http://www.google.com/search?q=my-service2", "http://someurl2.com"));
        serviceRepository.save(new Service("my-service3", "Description 2", "Someone Else", "http://www.google.com/search?q=my-service3", "http://someurl3.com"));

        // When
        List<Service> foundServices = serviceRepository.findByCreator("Johan");

        // Then
        assertThat(foundServices).containsOnlyOnce(savedService1, savedService2);
    }

    @Test public void
    deleting_service_that_doesnt_exists_returns_silently() {
        serviceRepository.delete("52d8d32eda06c15406ae8bad");
    }
}
