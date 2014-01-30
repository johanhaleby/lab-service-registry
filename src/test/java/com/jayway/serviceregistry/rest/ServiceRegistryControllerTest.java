package com.jayway.serviceregistry.rest;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.serviceregistry.boot.ServiceRegistryStart;
import com.jayway.serviceregistry.domain.Service;
import com.jayway.serviceregistry.domain.ServiceRepository;
import com.jayway.serviceregistry.infrastructure.security.ServiceRegistryUser;
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
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*;
import static com.jayway.restassured.module.mockmvc.matcher.RestAssuredMockMvcMatchers.endsWithPath;
import static com.jayway.restassured.module.mockmvc.matcher.RestAssuredMockMvcMatchers.startsWithPath;
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
        RestAssuredMockMvc.authentication = principal(new ServiceRegistryUser("johan.haleby@gmail.com", "Johan", "Haleby"));
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
        when().
                get(servicesLink).
        then().
                statusCode(200).
                root("_links.service.href[%d]").
                body(withArgs(0), startsWithPath("_links.self.href").and(endsWith("id1"))).
                body(withArgs(1), startsWithPath("_links.self.href").and(endsWith("id2")));
    }

    @Test public void
    service_returns_data_for_each_service_but_excludes_meta_and_supplement_when_they_dont_exist() {
        // Given
        givenServiceIsRegistered("id1", "service1", "Service Creator 1", "http://some-url.com/service1", "http://source1.com");
        givenServiceIsRegistered("id2", "service2", "Service Creator 2", "http://some-url.com/service2", "http://source2.com");
        String servicesLink = get().then().extract().path("_links.services.href");
        String linkToService2 = get(servicesLink).then().extract().path("_links.service.href.find { it.endsWith('%s') }", "id2");

        // When
        when().
                get(linkToService2).
        then().
                statusCode(200).
                body("serviceId", equalTo("id2")).
                body("description", equalTo("service2")).
                body("serviceUrl", equalTo("http://some-url.com/service2")).
                body("sourceUrl", equalTo("http://source2.com")).
                body("any { it.key == 'meta' }", is(false)).
                body("any { it.key == 'supplement' }", is(false));
    }

    @Test public void
    service_includes_link_to_self() {
        // Given
        givenServiceIsRegistered("id2", "service2", "Service Creator 2", "http://some-url.com/service2", "http://source2.com");
        String servicesLink = get().then().extract().path("_links.services.href");
        String linkToService = get(servicesLink).then().extract().path("_links.service.href");

        // When
        when().
                get(linkToService).
        then().
                statusCode(200).
                body("_links.self.href", endsWithPath("serviceId").and(startsWith("http://localhost/services/")));
    }

    @Test public void
    service_includes_supplement_when_defined_but_no_meta_even_when_defined() {
        // Given
        givenServiceIsRegistered("id2", "service2", "Service Creator 2", "http://some-url.com/service2", "http://source2.com", meta("type", "not-nice"),
                supplement("other", "stuff"), meta("x", "y"));
        String servicesLink = get().then().extract().path("_links.services.href");
        String linkToService = get(servicesLink).then().extract().path("_links.service.href");

        // When
        when().
                get(linkToService).
        then().
                statusCode(200).
                body("supplement", hasEntry("other", "stuff")).
                body("meta", nullValue());
    }

    @Test public void
    service_resource_returns_400_when_requested_service_is_not_registered() {
        // When
        when().
                get("/services/id9").
        then().
                statusCode(400).
                body("reason", equalTo("Cannot find service with serviceId id9"));
    }

    @SafeVarargs
    private final Service givenServiceIsRegistered(String id, String name, String creator, String serviceUrl, String sourceUrl, ImmutablePair<String, ImmutablePair<String, Object>>... data) {
        Service service = new Service(id, name, creator, serviceUrl, sourceUrl);
        if(data != null && data.length > 0) {
            Map<String, Object> metaMap = new HashMap<>();
            Map<String, Object> supplementMap = new HashMap<>();

            for (ImmutablePair<String, ImmutablePair<String, Object>> entry : data) {
                String tag = entry.getLeft();
                ImmutablePair<String, Object> pair = entry.getRight();
                switch (tag) {
                    case "meta":
                        metaMap.put(pair.getLeft(), pair.getRight());
                        break;
                    case "supplement":
                        supplementMap.put(pair.getLeft(), pair.getRight());
                        break;
                }
            }
            service.setSupplementaryMetaProperties(metaMap);
            service.setSupplementaryBodyProperties(supplementMap);
        }
        return serviceRepository.save(service);
    }

    private static ImmutablePair<String, ImmutablePair<String, Object>> meta(String name, Object value) {
        return new ImmutablePair<>("meta", new ImmutablePair<>(name, value));
    }

    private static ImmutablePair<String, ImmutablePair<String, Object>> supplement(String name, Object value) {
        return new ImmutablePair<>("supplement", new ImmutablePair<>(name, value));
    }
}
