package com.jayway.serviceregistry.messagebus.protocol;

public enum LogLevel {
    TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4);

    private final int level;

    private LogLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return Integer.toString(level);
    }
}