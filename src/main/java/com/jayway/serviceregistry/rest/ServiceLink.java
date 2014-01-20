package com.jayway.serviceregistry.rest;

import com.jayway.serviceregistry.domain.Service;
import org.springframework.hateoas.Link;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.bind.annotation.XmlAttribute;

class ServiceLink extends Link {

    private static final String SERVICE_REL = "service";

    @XmlAttribute
    String name;

    @XmlAttribute
    String createdBy;

    ServiceLink(Service service) {
        this(service, null);
    }

    ServiceLink(Service service, String username) {
        super(username == null ? service.getEntryPoint() : UriComponentsBuilder.fromHttpUrl(service.getEntryPoint()).queryParam("username", username).build().toUriString(), SERVICE_REL);
        this.name = service.getName();
        this.createdBy = service.getCreatedBy();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
