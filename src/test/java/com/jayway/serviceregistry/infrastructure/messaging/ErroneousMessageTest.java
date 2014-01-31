package com.jayway.serviceregistry.infrastructure.messaging;

import com.jayway.serviceregistry.infrastructure.messaging.protocol.Message;
import com.jayway.serviceregistry.infrastructure.messaging.support.ErroneousMessageTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Date;
import java.util.UUID;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.to;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class ErroneousMessageTest extends ErroneousMessageTestSupport{

    @Before public void
    given_a_new_rabbit_template_is_created() {
        // We create a new rabbit template that doesn't use our message converter so that we can simulate erroneous messages
        testTemplate = new RabbitTemplate(connectionFactory);
    }

    @Before public void
    given_log_queue_is_established() {
        logQueue = amqpAdmin.declareQueue();
        binding = BindingBuilder.bind(logQueue).to(lab).with(Topic.LOG.getRoutingKey());
        amqpAdmin.declareBinding(binding);
    }

    @After public void
    log_queue_is_removed_after_each_test() {
        amqpAdmin.deleteQueue(logQueue.getName());
        amqpAdmin.removeBinding(binding);
    }

    @Test public void
    logs_are_sent_when_message_is_not_json() {
        // When
        testTemplate.convertAndSend(Topic.getLabExchange(), "service", "error");

        // Then
        Object message = await().atMost(1, SECONDS).untilCall(to(amqpTemplate).receiveAndConvert(logQueue.getName()), notNullValue());
        assertThat(message).isInstanceOf(Message.class);

        Message msg = (Message) message;
        assertThat(msg.getBody()).containsEntry("message", "Couldn't parse message: error");
    }

    @Test public void
    logs_are_sent_when_message_is_missing_app_id() {
        // Given
        String messageId = UUID.randomUUID().toString();
        MessagePostProcessor props = givenMessageProperties(null, "streamId", new Date(), messageId, "ServiceOnlineEvent");
        String messageBody = "{\"createdBy\":\"Johan\",\"description\":\"This is the description of service1\",\"sourceUrl\":\"http://source1.com\",\"serviceUrl\":\"http://someurl1.com\"}";

        // When
        testTemplate.convertAndSend(Topic.getLabExchange(), "service", messageBody, props);

        // Then
        Object message = await().atMost(1, SECONDS).untilCall(to(amqpTemplate).receiveAndConvert(logQueue.getName()), notNullValue());
        assertThat(message).isInstanceOf(Message.class);

        Message msg = (Message) message;
        assertThat(msg.getBody()).containsEntry("message", "Erroneous message received: message " + messageId + " is missing required property appId.");
    }

    @Test public void
    logs_are_sent_when_message_is_missing_stream_id() {
        // Given
        String messageId = UUID.randomUUID().toString();
        MessagePostProcessor props = givenMessageProperties("appId", null, new Date(), messageId, "ServiceOnlineEvent");
        String messageBody = "{\"createdBy\":\"Johan\",\"description\":\"This is the description of service1\",\"sourceUrl\":\"http://source1.com\",\"serviceUrl\":\"http://someurl1.com\"}";

        // When
        testTemplate.convertAndSend(Topic.getLabExchange(), "service", messageBody, props);

        // Then
        Object message = await().atMost(1, SECONDS).untilCall(to(amqpTemplate).receiveAndConvert(logQueue.getName()), notNullValue());
        assertThat(message).isInstanceOf(Message.class);

        Message msg = (Message) message;
        assertThat(msg.getBody()).containsEntry("message", "Erroneous message received: message " + messageId + " is missing required property streamId.");
    }

    @Test public void
    logs_are_sent_when_message_is_missing_timestamp() {
        // Given
        String messageId = UUID.randomUUID().toString();
        MessagePostProcessor props = givenMessageProperties("appId", "streamId", null, messageId, "ServiceOnlineEvent");
        String messageBody = "{\"createdBy\":\"Johan\",\"description\":\"This is the description of service1\",\"sourceUrl\":\"http://source1.com\",\"serviceUrl\":\"http://someurl1.com\"}";

        // When
        testTemplate.convertAndSend(Topic.getLabExchange(), "service", messageBody, props);

        // Then
        Object message = await().atMost(1, SECONDS).untilCall(to(amqpTemplate).receiveAndConvert(logQueue.getName()), notNullValue());
        assertThat(message).isInstanceOf(Message.class);

        Message msg = (Message) message;
        assertThat(msg.getBody()).containsEntry("message", "Erroneous message received: message " + messageId + " is missing required property timestamp.");
    }

    @Test public void
    logs_are_sent_when_message_is_missing_message_id() {
        // Given
        MessagePostProcessor props = givenMessageProperties("appId", "streamId", new Date(), null, "ServiceOnlineEvent");
        String messageBody = "{\"createdBy\":\"Johan\",\"description\":\"This is the description of service1\",\"sourceUrl\":\"http://source1.com\",\"serviceUrl\":\"http://someurl1.com\"}";

        // When
        testTemplate.convertAndSend(Topic.getLabExchange(), "service", messageBody, props);

        // Then
        Object message = await().atMost(1, SECONDS).untilCall(to(amqpTemplate).receiveAndConvert(logQueue.getName()), notNullValue());
        assertThat(message).isInstanceOf(Message.class);

        Message msg = (Message) message;
        assertThat(msg.getBody()).containsEntry("message", "Erroneous message received: message is missing required property messageId.");
    }

    @Test public void
    logs_are_sent_when_message_is_missing_message_type() {
        // Given
        String messageId = UUID.randomUUID().toString();
        MessagePostProcessor props = givenMessageProperties("appId", "streamId", new Date(), messageId, null);
        String messageBody = "{\"createdBy\":\"Johan\",\"description\":\"This is the description of service1\",\"sourceUrl\":\"http://source1.com\",\"serviceUrl\":\"http://someurl1.com\"}";

        // When
        testTemplate.convertAndSend(Topic.getLabExchange(), "service", messageBody, props);

        // Then
        Object message = await().atMost(1, SECONDS).untilCall(to(amqpTemplate).receiveAndConvert(logQueue.getName()), notNullValue());
        assertThat(message).isInstanceOf(Message.class);

        Message msg = (Message) message;
        assertThat(msg.getBody()).containsEntry("message", "Erroneous message received: message " + messageId + " is missing required property type.");
    }
}
