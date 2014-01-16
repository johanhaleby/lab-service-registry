package com.jayway.serviceregistry.messagebus;

public enum Topic {
    LOG("lab.log"), GAME("lab.game"), SERVICE("lab.service");

    private final String exchange;

    Topic(String exchange) {
        this.exchange = exchange;
    }

    public String getExchange() {
        return exchange;
    }
}
