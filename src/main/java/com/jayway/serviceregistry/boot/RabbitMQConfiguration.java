package com.jayway.serviceregistry.boot;

import com.jayway.serviceregistry.messagebus.ServiceMessageReceiver;
import com.jayway.serviceregistry.messagebus.Topic;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitAccessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.RabbitConnectionFactoryProperties;

/**
 * Spring boot creates beans by default using the {@link org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration} class but we override the
 * {@link org.springframework.amqp.rabbit.connection.ConnectionFactory} here.
 */
@Configuration
class RabbitMQConfiguration {

    @Value("${amqp.connection.url:}")
    String amqpConnectionUri;

    @Autowired
    ServiceMessageReceiver serviceMessageReceiver;

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    MessageConverter messageConverter;

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange serviceTopicExchange() {
        return new TopicExchange(Topic.SERVICE.getExchange());
    }

    @Bean
    public Queue clientQueue() {
        return amqpAdmin.declareQueue();
    }

    /**
     * Binds to the service topic exchange
     */
    @Bean
    public Binding serviceTopicBinding() {
        return BindingBuilder.bind(clientQueue()).to(serviceTopicExchange()).with("anonymous.info");
    }

    @Bean
    public ConnectionFactory rabbitConnectionFactory() {
        RabbitConnectionFactoryProperties config = uriToProperties(amqpConnectionUri);
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(config.getHost());
        connectionFactory.setPort(config.getPort());

        if (config.getUsername() != null) {
            connectionFactory.setUsername(config.getUsername());
        }
        if (config.getPassword() != null) {
            connectionFactory.setPassword(config.getPassword());
        }
        if (config.getVirtualHost() != null) {
            connectionFactory.setVirtualHost(config.getVirtualHost());
        }

        return connectionFactory;
    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitConnectionFactory());
        container.setQueueNames(clientQueue().getName());
        container.setMessageListener(new MessageListenerAdapter(serviceMessageReceiver, jsonMessageConverter()));
        return container;
    }

    /*
     * Hack to convert an AMQP URL to a RabbitConnectionFactoryProperties class
     */

    static RabbitConnectionFactoryProperties uriToProperties(String uri) {
        RabbitConnectionFactoryProperties properties = new RabbitConnectionFactoryProperties();
        if (isNotEmpty(uri)) {
            String username = StringUtils.substringBetween(uri, "amqp://", ":");
            String password = StringUtils.substringBetween(uri, username + ":", "@");
            String hostWithPort = StringUtils.substringBetween(uri, "@", "/");

            // If no virtual host is specified
            if (isEmpty(hostWithPort)) {
                hostWithPort = StringUtils.substringAfter(uri, "@");
            }

            int port = properties.getPort();
            String host = hostWithPort;
            boolean hasPort = StringUtils.contains(hostWithPort, ":");
            if (hasPort) {
                host = StringUtils.substringBefore(hostWithPort, ":");
                port = NumberUtils.toInt(StringUtils.substringAfter(hostWithPort, ":"));
            }
            String virtualHost = StringUtils.substringAfter(uri, hostWithPort);


            properties.setUsername(username);
            properties.setPassword(password);
            properties.setHost(host);
            properties.setPort(port);

            if (isNotEmpty(virtualHost)) {
                properties.setVirtualHost(virtualHost);
            }

        }
        return properties;
    }

    @PostConstruct
    void setMessageConverterToRabbitTemplate() {
        rabbitTemplate.setMessageConverter(messageConverter);
    }
}