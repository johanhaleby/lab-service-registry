package com.jayway.serviceregistry.infrastructure.messaging;

public enum Topic {
    LOG("log"), GAME("game"), SERVICE("service");
    private static final String EXCHANGE = "lab";

    private final String routingKey;

    Topic(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public static String getLabExchange() {
        return EXCHANGE;
    }
}
