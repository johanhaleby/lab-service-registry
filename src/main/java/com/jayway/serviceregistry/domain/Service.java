package com.jayway.serviceregistry.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Service {

    @Id
    private String serviceId;
    @Indexed(unique = true)
    private String name;
    private String createdBy;
    private String entryPoint;
    private Map<String, Object> meta;

    public Service(String serviceId, String name, String createdBy, String entryPoint) {
        this.serviceId = serviceId;
        this.name = name;
        this.createdBy = createdBy;
        this.entryPoint = entryPoint;
        this.meta = new HashMap<>();
    }

    public Service(String name, String createdBy, String entryPoint) {
        this(UUID.randomUUID().toString(), name, createdBy, entryPoint);
    }

    public Service() {
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

    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;

        if (createdBy != null ? !createdBy.equals(service.createdBy) : service.createdBy != null) return false;
        if (entryPoint != null ? !entryPoint.equals(service.entryPoint) : service.entryPoint != null) return false;
        if (meta != null ? !meta.equals(service.meta) : service.meta != null) return false;
        if (name != null ? !name.equals(service.name) : service.name != null) return false;
        if (serviceId != null ? !serviceId.equals(service.serviceId) : service.serviceId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceId != null ? serviceId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (entryPoint != null ? entryPoint.hashCode() : 0);
        result = 31 * result + (meta != null ? meta.hashCode() : 0);
        return result;
    }
}
