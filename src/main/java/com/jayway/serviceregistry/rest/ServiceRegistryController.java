package com.jayway.serviceregistry.rest;

import com.jayway.serviceregistry.domain.Service;
import com.jayway.serviceregistry.domain.ServiceRepository;
import com.jayway.serviceregistry.infrastructure.security.ServiceRegistryUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.*;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
    private static final Logger log = LoggerFactory.getLogger(ServiceRegistryController.class);

    @Autowired
    HealthEndpoint healthEndpoint;
    @Autowired
    MetricsEndpoint metricsEndpoint;
    @Autowired
    TraceEndpoint traceEndpoint;
    @Autowired
    DumpEndpoint dumpEndpoint;
    @Autowired
    BeansEndpoint beansEndpoint;
    @Autowired
    AutoConfigurationReportEndpoint autoConfigurationReportEndpoint;

    @Autowired
    ServiceRepository serviceRepository;

    @RequestMapping(value = "/", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpEntity<Links> entryPoint() {
        log.trace("Entry point requested by {}", getUsername());
        Link selfLink = linkTo(methodOn(ServiceRegistryController.class).entryPoint()).withSelfRel();
        Link servicesLink = linkTo(methodOn(ServiceRegistryController.class).services()).withRel("services");
        Link healthLink = linkTo(ServiceRegistryController.class).slash(healthEndpoint.getId()).withRel("health");
        Link metricsLink = linkTo(ServiceRegistryController.class).slash(metricsEndpoint.getId()).withRel("metrics");
        Link traceLink = linkTo(ServiceRegistryController.class).slash(traceEndpoint.getId()).withRel("trace");
        Link dumpLink = linkTo(ServiceRegistryController.class).slash(dumpEndpoint.getId()).withRel("dump");
        Link beansLink = linkTo(ServiceRegistryController.class).slash(beansEndpoint.getId()).withRel("beans");
        Link autoConfigurationReportLink = linkTo(ServiceRegistryController.class).slash(autoConfigurationReportEndpoint.getId()).withRel("autoconfig");
        return new ResponseEntity<>(new Links(selfLink, servicesLink, healthLink, metricsLink, traceLink, dumpLink, beansLink, autoConfigurationReportLink), HttpStatus.OK);
    }

    @RequestMapping(value = "/services", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpEntity<Links> services() {
        log.trace("Services requested by {}", getUsername());
        List<Link> links = new ArrayList<>();
        List<Service> services = serviceRepository.findAll();
        links.add(linkTo(methodOn(ServiceRegistryController.class).services()).withSelfRel());
        for (Service service : services) {
            links.add(linkTo(methodOn(ServiceRegistryController.class).service(service.getServiceId())).withRel("service"));
        }
        return new ResponseEntity<>(new Links(links), HttpStatus.OK);
    }

    @RequestMapping(value = "/services/{serviceId}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpEntity<ServiceDTO> service(@PathVariable("serviceId") String serviceId) {
        log.trace("Service {} requested by {}", serviceId, getUsername());
        Service service = serviceRepository.findOne(serviceId);
        if (service == null) {
            throw new IllegalArgumentException("Cannot find service with serviceId " + serviceId);
        }
        Link selfLink = linkTo(methodOn(ServiceRegistryController.class).service(serviceId)).withSelfRel();
        return new ResponseEntity<>(new ServiceDTO(service, selfLink), HttpStatus.OK);
    }

    private String getUsername() {
        ServiceRegistryUser activeUser = (ServiceRegistryUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return activeUser.getUsername();
    }
}