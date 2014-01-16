package com.jayway.serviceregistry.domain;

public class Service {

    private String name;
    private String createdBy;
    private String entryPoint;

    public Service(String name, String createdBy, String entryPoint) {
        this.name = name;
        this.createdBy = createdBy;
        this.entryPoint = entryPoint;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;

        if (createdBy != null ? !createdBy.equals(service.createdBy) : service.createdBy != null) return false;
        if (entryPoint != null ? !entryPoint.equals(service.entryPoint) : service.entryPoint != null) return false;
        if (name != null ? !name.equals(service.name) : service.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (entryPoint != null ? entryPoint.hashCode() : 0);
        return result;
    }
}
