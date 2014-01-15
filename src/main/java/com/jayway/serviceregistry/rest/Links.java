package com.jayway.serviceregistry.rest;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

public class Links extends ResourceSupport {

    public Links(Link...links) {
        for (Link link : links) {
            add(link);
        }
    }
}