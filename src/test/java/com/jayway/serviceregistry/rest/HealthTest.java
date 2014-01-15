package com.jayway.serviceregistry.rest;

import com.jayway.serviceregistry.boot.ServiceRegistryStart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceRegistryStart.class)
@WebAppConfiguration
public class HealthTest {

    @Autowired
    protected WebApplicationContext wac;

    // @formatter:off

    @Test public void
    health_stats_are_provided_by_spring_actuator() {
        given().
                webAppContextSetup(wac).
        when().
                get("/health").
        then().
                statusCode(200).
                body(equalTo("ok"));
    }
    // @formatter:on
}
