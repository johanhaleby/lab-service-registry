package com.jayway.serviceregistry.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jayway.serviceregistry.domain.Service;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Unfortunately it's not easy to use Spring Link rendering and at the same time extend from HashMap without creating a custom serializer.
 */
@JsonInclude(NON_NULL)
class ServiceDTO extends ResourceSupport {

    @XmlAttribute
    String serviceId;

    @XmlAttribute
    String description;

    @XmlAttribute
    String createdBy;


    @XmlAttribute
    String serviceUrl;

    @XmlAttribute
    String sourceUrl;

    @XmlAttribute
    Map<String, Object> supplement;

    @XmlAttribute
    Map<String, Object> meta;

    ServiceDTO(Service service, Link... links) {
        for (Link link : links) {
            add(link);
        }
        this.serviceId = service.getServiceId();
        this.description = service.getDescription();
        this.createdBy = service.getCreatedBy();
        this.serviceUrl = service.getServiceUrl();
        this.sourceUrl = service.getSourceUrl();
        this.supplement = service.getOptionalProperties();
        if (this.supplement.isEmpty()) {
            this.supplement = null;
        }
        this.meta = service.getMeta();
        if (this.meta.isEmpty()) {
            this.meta = null;
        }
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public Map<String, Object> getSupplement() {
        return supplement;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }
}
