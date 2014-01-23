package com.jayway.serviceregistry.rest;

import com.jayway.serviceregistry.domain.Service;
import org.springframework.hateoas.Link;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Map;

class ServiceLink extends Link {

    private static final String SERVICE_REL = "service";

    @XmlAttribute
    String name;

    @XmlAttribute
    String createdBy;

    @XmlAttribute
    String streamId;

    @XmlAttribute
    Map<String, Object> meta;

    ServiceLink(Service service) {
        this(service, null);
    }

    ServiceLink(Service service, String username) {
        super(username == null ? service.getServiceUrl() : UriComponentsBuilder.fromHttpUrl(service.getServiceUrl()).queryParam("username", username).build().toUriString(), SERVICE_REL);
        this.name = service.getDescription();
        this.createdBy = service.getCreatedBy();
        this.streamId = service.getServiceId();
        this.meta = service.getMeta();
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
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
