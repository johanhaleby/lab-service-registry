package com.jayway.serviceregistry.rest;

import com.jayway.serviceregistry.domain.Service;
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
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class ServiceRegistryController {

    @Autowired
    HealthEndpoint healthEndpoint;
    @Autowired
    MetricsEndpoint metricsEndpoint;

    @RequestMapping(value = "/", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpEntity<Links> entryPoint() {
        Link selfLink = linkTo(methodOn(ServiceRegistryController.class).entryPoint()).withSelfRel();
        Link servicesLink = linkTo(methodOn(ServiceRegistryController.class).services()).withRel("services");
        Link healthLink = linkTo(ServiceRegistryController.class).slash(healthEndpoint.getId()).withRel("health");
        Link metricsLink = linkTo(ServiceRegistryController.class).slash(metricsEndpoint.getId()).withRel("metrics");
        return new ResponseEntity<>(new Links(selfLink, servicesLink, healthLink, metricsLink), HttpStatus.OK);
    }

    @RequestMapping(value = "/services", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpEntity<Links> services() {
        Service service1 = new Service("service1", "Service Creator 1", "http://some-url.com/service1");
        Service service2 = new Service("service2", "Service Creator 2", "http://some-url.com/service2");
        Link selfLink = linkTo(methodOn(ServiceRegistryController.class).services()).withSelfRel();
        Link serviceLink1 = new ServiceLink(service1);
        Link serviceLink2 = new ServiceLink(service2);
        return new ResponseEntity<>(new Links(selfLink, serviceLink1, serviceLink2), HttpStatus.OK);
    }
}
