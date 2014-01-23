package com.jayway.serviceregistry.domain;

import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

public class Service {

    @Id
    private String serviceId;
    private String description;
    private String createdBy;
    private String serviceUrl;
    private String sourceUrl;
    private Map<String, Object> meta;
    private Map<String, Object> optionalProperties;

    public Service(String serviceId, String description, String createdBy, String serviceUrl, String sourceUrl) {
        this(serviceId, description, createdBy, serviceUrl, sourceUrl, new HashMap<String, Object>(), new HashMap<String, Object>());
    }

    public Service(String serviceId, String description, String createdBy, String serviceUrl, String sourceUrl, Map<String, Object> optionalProperties, Map<String, Object> meta) {
        this.serviceId = serviceId;
        this.description = description;
        this.createdBy = createdBy;
        this.serviceUrl = serviceUrl;
        this.sourceUrl = sourceUrl;
        this.meta = new HashMap<>(meta);
        this.optionalProperties = new HashMap<>(optionalProperties);
    }

    public Service() {
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public Map<String, Object> getOptionalProperties() {
        return optionalProperties;
    }

    public void setOptionalProperties(Map<String, Object> optionalProperties) {
        this.optionalProperties = optionalProperties;
    }
}

