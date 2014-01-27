package com.jayway.serviceregistry.messagebus.protocol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.jayway.serviceregistry.messagebus.protocol.EventType.*;

public class Messages {

    public static Map<String, Object> gameCreatedEvent(String gameId, String createdBy, String gameUrl, List<String> players) {
        Map<String, Object> body = new HashMap<>();
        body.put("createdBy", createdBy);
        body.put("players", players);
        body.put("gameType", "rock-paper-scissors");
        body.put("gameUrl", gameUrl);

        return createMessage(gameId, GAME_CREATED_EVENT, body, new HashMap<String, Object>());
    }

    public static Map<String, Object> gameEndedEvent(String gameId, Map<String, Number> scores) {
        Map<String, Object> body = new HashMap<>();
        body.put("scores", scores);
        body.put("result", "true");
        body.put("gameType", "rock-paper-scissors");

        return createMessage(gameId, GAME_ENDED_EVENT, body, new HashMap<String, Object>());
    }

    public static Map<String, Object> serviceOnlineEvent(String serviceId, String description, String createdBy, String serviceUrl, String sourceUrl) {
        return serviceOnlineEvent(serviceId, description, createdBy, serviceUrl, sourceUrl, new HashMap<String, Object>());
    }

    public static Map<String, Object> serviceOnlineEvent(String serviceId, String description, String createdBy, String serviceUrl, String sourceUrl, Map<String, Object> meta) {
        Map<String, Object> body = new HashMap<>();
        body.put("description", description);
        body.put("createdBy", createdBy);
        body.put("serviceUrl", serviceUrl);
        body.put("sourceUrl", sourceUrl);

        return createMessage(serviceId, SERVICE_ONLINE_EVENT, body, meta);
    }

    public static Map<String, Object> logEvent(LogLevel level, String context, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("level", level);
        body.put("context", context);
        body.put("message", message);
        return createMessage(null, LOG_EVENT, body, new HashMap<String, Object>());
    }

    public static Map<String, Object> logEvent(LogLevel level, String context, String message, Map<String, Object> meta) {
        Map<String, Object> body = new HashMap<>();
        body.put("level", level);
        body.put("context", context);
        body.put("message", message);
        return createMessage(null, LOG_EVENT, body, meta);
    }

    public static Map<String, Object> serviceOfflineEvent(String serviceId) {
        Map<String, Object> body = new HashMap<>();
        return createMessage(serviceId, SERVICE_OFFLINE_EVENT, body, new HashMap<String, Object>());
    }

    private static Map<String, Object> createMessage(String streamId, String type, Map<String, Object> body, Map<String, Object> meta) {
        Map<String, Object> message = new HashMap<String, Object>();
        if (meta == null) {
            meta = new HashMap<>();
        }
        message.put("type", type);
        message.put("body", body);
        message.put("meta", meta);
        if (streamId != null) {
            message.put("streamId", streamId);
        }
        message.put("messageId", UUID.randomUUID().toString());
        message.put("createdAt", System.currentTimeMillis());
        return message;
    }
}