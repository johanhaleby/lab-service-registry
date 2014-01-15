package com.jayway.serviceregistry.rest;

import com.jayway.serviceregistry.boot.ServiceRegistryStart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import static com.jayway.restassured.RestAssured.withArgs;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceRegistryStart.class)
@WebAppConfiguration
public class EntryPointControllerTest {
    @Autowired
    WebApplicationContext wac;

    @Test public void
    entry_point_returns_links() {
        given().
                webAppContextSetup(wac).
        when().
                get("/").
        then().
                statusCode(200).
                root("_links.%s.href").
                body(withArgs("self"), notNullValue()).
                body(withArgs("health"), endsWith("/health")).
                body(withArgs("metrics"), endsWith("/metrics"));
    }
}
