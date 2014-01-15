package com.jayway.serviceregistry.interfaces.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class EntryPointController {

    @Autowired
    HealthEndpoint healthEndpoint;
    @Autowired
    MetricsEndpoint metricsEndpoint;

    @RequestMapping(value = "/", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpEntity<Links> entryPoint() {
        Link selfLink = linkTo(EntryPointController.class).withSelfRel();
        Link healthLink = linkTo(EntryPointController.class).slash(healthEndpoint.getPath()).withRel("health");
        Link metricsLink = linkTo(EntryPointController.class).slash(metricsEndpoint.getPath()).withRel("metrics");
        return new ResponseEntity<>(new Links(selfLink, healthLink, metricsLink), HttpStatus.OK);
    }
}
