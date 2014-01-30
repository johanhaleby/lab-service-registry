package com.jayway.serviceregistry.infrastructure.messaging;

import com.jayway.serviceregistry.boot.ServiceRegistryStart;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.to;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceRegistryStart.class)
public class ErroneousMessageTest {

    RabbitTemplate testTemplate;

    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    CachingConnectionFactory connectionFactory;

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    TopicExchange lab;

    @Before public void
    given_a_new_rabbit_template_is_created() {
        // We create a new rabbit template that doesn't use our message converter so that we can simulate erroneous messages
        testTemplate = new RabbitTemplate(connectionFactory);
    }

    @Test @SuppressWarnings("unchecked") public void
    logs_are_sent_when_message_is_erroneous()  {
        // Given
        Queue logQueue = amqpAdmin.declareQueue();
        Binding binding = BindingBuilder.bind(logQueue).to(lab).with(Topic.LOG.getRoutingKey());
        amqpAdmin.declareBinding(binding);

        try {
            // When
            testTemplate.convertAndSend(Topic.getLabExchange(), "service", "error");

            // Then
            Object message = await().atMost(1, SECONDS).untilCall(to(amqpTemplate).receiveAndConvert(logQueue.getName()), notNullValue());
            assertThat(message).isInstanceOf(Message.class);

            Message msg = (Message) message;
            assertThat(msg.getBody()).containsEntry("message", "Couldn't parse message: error");
        } finally {
            amqpAdmin.deleteQueue(logQueue.getName());
            amqpAdmin.removeBinding(binding);
        }
    }
}
