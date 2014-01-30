package com.jayway.serviceregistry.infrastructure.messaging.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.serviceregistry.infrastructure.messaging.MessageSender;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.Messages;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.ServiceRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.serviceregistry.infrastructure.messaging.Topic.LOG;
import static com.jayway.serviceregistry.infrastructure.messaging.protocol.LogLevel.ERROR;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * We create our own MessageConverter because Springs message converter adds additional headers such as specifying the class type in order to
 * serialize and deserialize correctly. This is not needed in our Lab so we role our own. This converter also makes sure that the required message headers
 * are available in each message.
 */
public class LabMessageConverter implements MessageConverter {
    private static final Logger log = LoggerFactory.getLogger(LabMessageConverter.class);

    final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageSender messageSender;

    public LabMessageConverter(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        if (!(object instanceof com.jayway.serviceregistry.infrastructure.messaging.protocol.Message)) {
            throw new IllegalArgumentException(format("Cannot send object because it's not an instance of %s (was %s).",
                    com.jayway.serviceregistry.infrastructure.messaging.protocol.Message.class.getName(), object == null ? null : object.getClass().getName()));
        }
        com.jayway.serviceregistry.infrastructure.messaging.protocol.Message msg = (com.jayway.serviceregistry.infrastructure.messaging.protocol.Message) object;
        applyMetaProperties(msg, messageProperties);

        byte[] obj;
        try {
            obj = objectMapper.writeValueAsBytes(msg.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        messageProperties.setContentType("application/json");
        return new Message(obj, messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        try {
            Map<String, Object> body = objectMapper.readValue(message.getBody(), new TypeReference<Map<String, Object>>() {});
            MessageProperties props = message.getMessageProperties();
            String messageId = assertNotBlank("messageId", props.getMessageId());
            String appId = assertNotBlank(messageId, "appId", props.getAppId());
            String streamId = assertNotBlank(messageId, "streamId", getHeader(props, "streamId", String.class));
            String type = assertNotBlank(messageId, "type", props.getType());
            long timestamp = assertNotNull(messageId, "timestamp", props.getTimestamp()).getTime();

            Map<String, Object> meta = new HashMap<>();
            meta.put("messageId", messageId);
            meta.put("appId", appId);
            meta.put("streamId", streamId);
            meta.put("type", type);
            meta.put("timestamp", timestamp);

            // Add additional headers
            for (Map.Entry<String, Object> header : props.getHeaders().entrySet()) {
                if (!header.getKey().equals("streamId")) {
                    meta.put(header.getKey(), header.getValue());
                }
            }

            return new com.jayway.serviceregistry.infrastructure.messaging.protocol.Message(meta, body);
        } catch (IOException e) {
            try {
                String messageAsString = StringUtils.toString(message.getBody(), "UTF-8");
                messageSender.sendMessage(LOG, Messages.log(ERROR, ServiceRegistry.APP_ID, "Couldn't parse message: " + messageAsString));
                log.info("Erroneous message received: " + messageAsString);
            } catch (UnsupportedEncodingException uee) {
                log.info("Erroneous message with invalid encoding received.");
                messageSender.sendMessage(LOG, Messages.log(ERROR, ServiceRegistry.APP_ID, "Couldn't parse message since it was invalid and not encoded as UTF-8"));
            }
        } catch (ClassCastException e) {
            log.info("Erroneous message received: {}", e.getMessage());
        } catch (IllegalStateException e) {
            log.info(e.getMessage());
        }
        return null;
    }

    private String assertNotBlank(String requiredPropertyName, String value) {
        if (isBlank(value)) {
            String msg = format("Erroneous message received: message is missing required property %s.", requiredPropertyName);
            messageSender.sendMessage(LOG, Messages.log(ERROR, ServiceRegistry.APP_ID, msg));
            throw new IllegalStateException(msg);
        }
        return value;
    }

    private String assertNotBlank(String messageId, String requiredPropertyName, String value) {
        if (isBlank(value)) {
            return sendErrorBecauseOfMissingRequiredProperty(messageId, requiredPropertyName);
        }
        return value;
    }

    private <T> T assertNotNull(String messageId, String requiredPropertyName, T value) {
        if (value == null) {
            sendErrorBecauseOfMissingRequiredProperty(messageId, requiredPropertyName);
        }
        return value;
    }

    private String sendErrorBecauseOfMissingRequiredProperty(String messageId, String requiredPropertyName) {
        String msg = format("Erroneous message received: message %s is missing required property %s.", messageId, requiredPropertyName);
        messageSender.sendMessage(LOG, Messages.log(ERROR, ServiceRegistry.APP_ID, msg));
        throw new IllegalStateException(msg);
    }

    private <T> T getHeader(MessageProperties props, String key, Class<T> cls) {
        Object o = props.getHeaders().get(key);
        try {
            return cls.cast(o);
        } catch (ClassCastException e) {
            String errorMessage = format("Couldn't parse message since property %s was not of type %s.", key, cls.getSimpleName());
            messageSender.sendMessage(LOG, Messages.log(ERROR, ServiceRegistry.APP_ID, errorMessage));
            throw new ClassCastException(errorMessage);
        }
    }

    private void applyMetaProperties(com.jayway.serviceregistry.infrastructure.messaging.protocol.Message msg, MessageProperties messageProperties) {
        Map<String, Object> meta = msg.getMeta();
        String type = findAndRemoveRequiredValueWithKey(meta, "type");
        String messageId = findAndRemoveRequiredValueWithKey(meta, "messageId");
        long timestamp = findAndRemoveRequiredValueWithKey(meta, "timestamp");
        String streamId = findAndRemoveRequiredValueWithKey(meta, "streamId");
        String appId = findAndRemoveRequiredValueWithKey(meta, "appId");
        messageProperties.setAppId(appId);
        messageProperties.setContentType("application/json");
        messageProperties.setMessageId(messageId);
        messageProperties.setTimestamp(new Date(timestamp));
        messageProperties.setHeader("streamId", streamId);
        messageProperties.setType(type);

        for (Map.Entry<String, Object> metaEntry : meta.entrySet()) {
            messageProperties.setHeader(metaEntry.getKey(), metaEntry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T findAndRemoveRequiredValueWithKey(Map<String, Object> meta, String key) {
        Object value = meta.remove(key);
        if (value == null) {
            throw new IllegalArgumentException(format("Required value for key %s was not defined", key));
        }

        return (T) value;
    }
}
