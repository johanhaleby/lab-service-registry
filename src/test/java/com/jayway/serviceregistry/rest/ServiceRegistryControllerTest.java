package com.jayway.serviceregistry.rest;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.serviceregistry.boot.ServiceRegistryStart;
import com.jayway.serviceregistry.domain.Service;
import com.jayway.serviceregistry.domain.ServiceRepository;
import com.jayway.serviceregistry.security.ServiceRegistryUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import static com.jayway.restassured.RestAssured.withArgs;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.get;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceRegistryStart.class)
@WebAppConfiguration
public class ServiceRegistryControllerTest {
    @Autowired
    WebApplicationContext wac;

    @Autowired
    ServiceRepository serviceRepository;

    @Before public void
    given_rest_assured_is_configured_with_the_web_application_context() {
        RestAssuredMockMvc.webAppContextSetup(wac);
        RestAssuredMockMvc.basePath = "/";
    }

    @After
    public void
    rest_assured_is_reset_after_each_test() {
        RestAssuredMockMvc.reset();
    }


    @Before @After
    public void drop_mongo_service_collection() throws Exception {
       serviceRepository.deleteAll();
    }

    @Test public void
    entry_point_returns_links_to_subresources() {
        get().then().
                statusCode(200).
                root("_links.%s.href").
                body(withArgs("self"), notNullValue()).
                body(withArgs("services"), endsWith("/services")).
                body(withArgs("health"), endsWith("/health")).
                body(withArgs("metrics"), endsWith("/metrics"));
    }

    @Test public void
    subresources_returns_links_to_defined_services_and_self() {
        // Given
        serviceRepository.save(new Service("id1", "service1", "Service Creator 1", "http://some-url.com/service1"));
        serviceRepository.save(new Service("id2", "service2", "Service Creator 2", "http://some-url.com/service2"));

        String servicesLink = get().then().extract().path("_links.services.href");

        given().
                auth().principal(new ServiceRegistryUser("johan.haleby@gmail.com", "Johan", "Haleby")).
        when().
                get(servicesLink).
        then().
                statusCode(200).
                root("_links.service.find { it.name == '%s'}").
                body("href", withArgs("service1"), equalTo("http://some-url.com/service1")).
                body("createdBy", withArgs("service1"), equalTo("Service Creator 1")).
                body("streamId", withArgs("service1"), equalTo("id1")).
                body("href", withArgs("service2"), equalTo("http://some-url.com/service2")).
                body("createdBy", withArgs("service2"), equalTo("Service Creator 2")).
                body("streamId", withArgs("service2"), equalTo("id2"));
    }
}
