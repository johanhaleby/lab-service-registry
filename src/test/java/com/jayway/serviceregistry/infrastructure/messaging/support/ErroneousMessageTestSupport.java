package com.jayway.serviceregistry.infrastructure.messaging.support;

import com.jayway.serviceregistry.boot.ServiceRegistryStart;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceRegistryStart.class)
@Ignore("Not a test")
public class ErroneousMessageTestSupport {

    protected RabbitTemplate testTemplate;

    @Autowired
    protected AmqpTemplate amqpTemplate;

    @Autowired
    protected CachingConnectionFactory connectionFactory;

    @Autowired
    protected AmqpAdmin amqpAdmin;

    @Autowired
    protected TopicExchange lab;

    protected Queue logQueue;

    protected Binding binding;

    protected MessagePostProcessor givenMessageProperties(final String appId, final String streamId, final Date date, final String messageId, final String type) {
        return new MessagePostProcessor() {
            @Override
            public org.springframework.amqp.core.Message postProcessMessage(org.springframework.amqp.core.Message message) throws AmqpException {
                MessageProperties props = message.getMessageProperties();
                if (appId != null) props.setAppId(appId);
                if (streamId != null) props.setHeader("streamId", streamId);
                if (date != null) props.setTimestamp(date);
                if (messageId != null) props.setMessageId(messageId);
                if (type != null) props.setType(type);
                return message;
            }
        };
    }
}
