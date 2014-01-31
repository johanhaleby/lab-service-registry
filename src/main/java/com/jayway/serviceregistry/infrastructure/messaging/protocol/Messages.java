package com.jayway.serviceregistry.infrastructure.messaging.protocol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.jayway.serviceregistry.infrastructure.messaging.protocol.MessageType.*;
import static com.jayway.serviceregistry.infrastructure.messaging.protocol.ServiceRegistry.MACHINE_ID;


public class Messages {

    public static Message gameCreated(String appId, String gameId, String createdBy, String gameUrl, List<String> players) {
        Map<String, Object> body = new HashMap<>();
        body.put("createdBy", createdBy);
        body.put("players", players);
        body.put("gameType", "rock-paper-scissors");
        body.put("gameUrl", gameUrl);

        return createMessage(appId, gameId, GAME_CREATED, body, new HashMap<String, Object>());
    }

    public static Message gameEnded(String appId, String gameId, Map<String, Number> scores) {
        Map<String, Object> body = new HashMap<>();
        body.put("scores", scores);
        body.put("gameType", "rock-paper-scissors");

        return createMessage(appId, gameId, GAME_ENDED, body, new HashMap<String, Object>());
    }

    public static Message serviceOnline(String serviceId, String description, String createdBy, String serviceUrl, String sourceUrl) {
        return serviceOnline(serviceId, description, createdBy, serviceUrl, sourceUrl, new HashMap<String, Object>());
    }

    public static Message serviceOnline(String serviceId, String description, String createdBy, String serviceUrl, String sourceUrl, Map<String, Object> meta) {
        Map<String, Object> body = new HashMap<>();
        body.put("description", description);
        body.put("createdBy", createdBy);
        body.put("serviceUrl", serviceUrl);
        body.put("sourceUrl", sourceUrl);

        return createMessage(serviceId, serviceId, SERVICE_ONLINE, body, meta);
    }

    public static Message log(LogLevel level, String appId, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("level", level);
        body.put("message", message);
        return createMessage(appId, MACHINE_ID, LOG, body, new HashMap<String, Object>());
    }

    public static Message log(LogLevel level, String appId, String message, Map<String, Object> meta) {
        Map<String, Object> body = new HashMap<>();
        body.put("level", level);
        body.put("message", message);
        return createMessage(appId, MACHINE_ID, LOG, body, meta);
    }

    public static Message serviceOffline(String serviceId) {
        Map<String, Object> body = new HashMap<>();
        return createMessage(serviceId, serviceId, SERVICE_OFFLINE, body, new HashMap<String, Object>());
    }

    private static Message createMessage(String appId, String streamId, String type, Map<String, Object> body, Map<String, Object> meta) {
        Map<String, Object> messageMeta = new HashMap<>();
        messageMeta.put("appId", appId);
        messageMeta.put("streamId", streamId);
        messageMeta.put("type", type);
        messageMeta.put("messageId", UUID.randomUUID().toString());
        messageMeta.put("timestamp", System.currentTimeMillis());
        if (meta == null) {
            meta = new HashMap<>();
        }
        messageMeta.putAll(meta);

        Map<String, Object> messageBody = new HashMap<>();
        messageBody.putAll(body);

        return new Message(messageMeta, messageBody);
    }
}