package com.jayway.serviceregistry.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServiceRepository extends MongoRepository<Service, String> {

    List<Service> findByCreatedBy(String createdBy);

    Service findByName(String name);
}
