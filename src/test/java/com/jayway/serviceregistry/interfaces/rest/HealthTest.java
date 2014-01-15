package com.jayway.serviceregistry.interfaces.rest;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.serviceregistry.boot.ApplicationStart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.get;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationStart.class)
@WebAppConfiguration
public class HealthTest {

    @Autowired
    protected WebApplicationContext wac;

    // @formatter:off

    @Before public void
    configure_mock_mvc_instance() {
        RestAssuredMockMvc.webAppContextSetup(wac);
    }

    @After public void
    reset_rest_assured() {
        RestAssuredMockMvc.reset();
    }

    @Test public void
    health_stats_are_provided_by_spring_actuator() {
        get("/health").then().assertThat().body(equalTo("ok")).and().statusCode(200);
    }
    // @formatter:on
}
