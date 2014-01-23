package com.jayway.serviceregistry.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ServiceRepository extends MongoRepository<Service, String> {

    @Query("{ 'body.createdBy' : ?0 }")
    List<Service> findByCreator(String creator);
}
