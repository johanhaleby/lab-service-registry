package com.jayway.serviceregistry.domain;

import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

public class Service {

    @Id
    private String serviceId;
    private Map<String, Object> meta;
    private Map<String, Object> body;

    public Service(String serviceId, String description, String createdBy, String serviceUrl, String sourceUrl) {
        this.serviceId = serviceId;
        this.meta = new HashMap<>();
        this.body = new HashMap<>();
        body.put("description", description);
        body.put("createdBy", createdBy);
        body.put("serviceUrl", serviceUrl);
        body.put("sourceUrl", sourceUrl);
    }

    public Service(String serviceId, Map<String, Object> body, Map<String, Object> meta) {
        this.serviceId = serviceId;
        this.meta = new HashMap<>(meta);
        this.body = new HashMap<>(body);
    }

    public Service() {
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;

        if (body != null ? !body.equals(service.body) : service.body != null) return false;
        if (meta != null ? !meta.equals(service.meta) : service.meta != null) return false;
        if (serviceId != null ? !serviceId.equals(service.serviceId) : service.serviceId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceId != null ? serviceId.hashCode() : 0;
        result = 31 * result + (meta != null ? meta.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }

    public String getDescription() {
        return (String) body.get("description");
    }

    public String getCreatedBy() {
        return (String) body.get("createdBy");
    }

    public String getServiceUrl() {
        return (String) body.get("serviceUrl");
    }

    public String getSourceUrl() {
        return (String) body.get("sourceUrl");
    }
}

