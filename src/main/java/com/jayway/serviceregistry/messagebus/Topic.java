package com.jayway.serviceregistry.messagebus;

public enum Topic {
    LOG("lab.log"), GAME("lab.game"), SERVICE("lab.service");
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
