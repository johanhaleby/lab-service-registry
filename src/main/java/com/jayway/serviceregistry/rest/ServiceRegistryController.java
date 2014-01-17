package com.jayway.serviceregistry.rest;

import com.jayway.serviceregistry.domain.Service;
import com.jayway.serviceregistry.domain.ServiceRepository;
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

import java.util.ArrayList;
import java.util.List;

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
    @Autowired
    ServiceRepository serviceRepository;

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
        List<Link> links = new ArrayList<>();
        List<Service> services = serviceRepository.findAll();
        links.add(linkTo(methodOn(ServiceRegistryController.class).services()).withSelfRel());
        for (Service service : services) {
            links.add(new ServiceLink(service));
        }
        return new ResponseEntity<>(new Links(links), HttpStatus.OK);
    }
}