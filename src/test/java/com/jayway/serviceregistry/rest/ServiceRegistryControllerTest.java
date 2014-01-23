package com.jayway.serviceregistry.rest;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.serviceregistry.boot.ServiceRegistryStart;
import com.jayway.serviceregistry.domain.Service;
import com.jayway.serviceregistry.domain.ServiceRepository;
import com.jayway.serviceregistry.security.ServiceRegistryUser;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

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
                body(withArgs("metrics"), endsWith("/metrics")).
                body(withArgs("beans"), endsWith("/beans")).
                body(withArgs("dump"), endsWith("/dump")).
                body(withArgs("trace"), endsWith("/trace")).
                body(withArgs("autoconfig"), endsWith("/autoconfig"));
    }

    @Test public void
    services_returns_links_to_defined_services_and_self() {
        // Given
        givenServiceIsRegistered("id1", "service1", "Service Creator 1", "http://some-url.com/service1", "http://source1.com");
        givenServiceIsRegistered("id2", "service2", "Service Creator 2", "http://some-url.com/service2", "http://source2.com");

        String servicesLink = get().then().extract().path("_links.services.href");

        // When

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
                body("meta.size()", withArgs("service1"), is(0)).
                body("href", withArgs("service2"), equalTo("http://some-url.com/service2")).
                body("createdBy", withArgs("service2"), equalTo("Service Creator 2")).
                body("streamId", withArgs("service2"), equalTo("id2")).
                body("meta.size()", withArgs("service2"), is(0));
    }

    @Test public void
    services_returns_meta_data_for_each_service_if_defined() {
        // Given
        givenServiceIsRegistered("id1", "service1", "Service Creator 1", "http://some-url.com/service1", "http://source1.com", meta("type", "nice"), meta("ttl", "2"));
        givenServiceIsRegistered("id2", "service2", "Service Creator 2", "http://some-url.com/service2", "http://source2.com", meta("type", "not-nice"), meta("other", "stuff"), meta("x", "y"));

        String servicesLink = get().then().extract().path("_links.services.href");

        // When
        given().
                auth().principal(new ServiceRegistryUser("johan.haleby@gmail.com", "Johan", "Haleby")).
        when().
                get(servicesLink).
        then().
                statusCode(200).
                root("_links.service.find { it.name == '%s'}").
                body("meta", withArgs("service1"), allOf(hasEntry("type", "nice"), hasEntry("ttl", "2"))).
                body("meta", withArgs("service2"), allOf(hasEntry("type", "not-nice"), hasEntry("other", "stuff"), hasEntry("x", "y")));
    }


    @SafeVarargs
    private final Service givenServiceIsRegistered(String id, String name, String creator, String serviceUrl, String sourceUrl, Map.Entry<String, Object>... meta) {
        Service service = new Service(id, name, creator, serviceUrl, sourceUrl);
        if(meta != null && meta.length > 0) {
            Map<String, Object> metaMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : meta) {
                metaMap.put(entry.getKey(), entry.getValue());
            }
            service.setMeta(metaMap);
        }
        return serviceRepository.save(service);
    }

    private static Map.Entry<String, Object> meta(String name, Object value) {
        return new ImmutablePair<>(name, value);
    }
}
