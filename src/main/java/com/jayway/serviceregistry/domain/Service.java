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
    private Map<String, Object> supplementaryBodyProperties;
    private Map<String, Object> supplementaryMetaProperties;

    public Service(String serviceId, String description, String createdBy, String serviceUrl, String sourceUrl) {
        this(serviceId, description, createdBy, serviceUrl, sourceUrl, new HashMap<String, Object>(), new HashMap<String, Object>());
    }

    public Service(String serviceId, String description, String createdBy, String serviceUrl, String sourceUrl, Map<String, Object> supplementaryBodyProperties,
                   Map<String, Object> supplementaryMetaProperties) {
        this.serviceId = serviceId;
        this.description = description;
        this.createdBy = createdBy;
        this.serviceUrl = serviceUrl;
        this.sourceUrl = sourceUrl;
        this.supplementaryBodyProperties = new HashMap<>(supplementaryBodyProperties);
        this.supplementaryMetaProperties = new HashMap<>(supplementaryMetaProperties);
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

    public Map<String, Object> getSupplementaryBodyProperties() {
        return supplementaryBodyProperties;
    }

    public void setSupplementaryBodyProperties(Map<String, Object> supplementaryBodyProperties) {
        this.supplementaryBodyProperties = supplementaryBodyProperties;
    }

    public Map<String, Object> getSupplementaryMetaProperties() {
        return supplementaryMetaProperties;
    }

    public void setSupplementaryMetaProperties(Map<String, Object> supplementaryMetaProperties) {
        this.supplementaryMetaProperties = supplementaryMetaProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;

        if (createdBy != null ? !createdBy.equals(service.createdBy) : service.createdBy != null) return false;
        if (description != null ? !description.equals(service.description) : service.description != null) return false;
        if (supplementaryMetaProperties != null ? !supplementaryMetaProperties.equals(service.supplementaryMetaProperties) : service.supplementaryMetaProperties != null) return false;
        if (serviceId != null ? !serviceId.equals(service.serviceId) : service.serviceId != null) return false;
        if (serviceUrl != null ? !serviceUrl.equals(service.serviceUrl) : service.serviceUrl != null) return false;
        if (sourceUrl != null ? !sourceUrl.equals(service.sourceUrl) : service.sourceUrl != null) return false;
        if (supplementaryBodyProperties != null ? !supplementaryBodyProperties.equals(service.supplementaryBodyProperties) : service.supplementaryBodyProperties != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceId != null ? serviceId.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (serviceUrl != null ? serviceUrl.hashCode() : 0);
        result = 31 * result + (sourceUrl != null ? sourceUrl.hashCode() : 0);
        result = 31 * result + (supplementaryBodyProperties != null ? supplementaryBodyProperties.hashCode() : 0);
        result = 31 * result + (supplementaryMetaProperties != null ? supplementaryMetaProperties.hashCode() : 0);
        return result;
    }
}

