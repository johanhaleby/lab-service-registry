package com.jayway.serviceregistry.infrastructure.messaging.protocol;

import java.util.Map;

public class Message {

    private Map<String, Object> meta;
    private Map<String, Object> body;

    public Message(Map<String, Object> meta, Map<String, Object> body) {
        this.meta = meta;
        this.body = body;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public <T> T meta(String key) {
        return (T) meta.get(key);
    }

    public <T> T body(String key) {
        return (T) body.get(key);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Message{");
        sb.append("meta=").append(meta);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (body != null ? !body.equals(message.body) : message.body != null) return false;
        if (meta != null ? !meta.equals(message.meta) : message.meta != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = meta != null ? meta.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }
}
