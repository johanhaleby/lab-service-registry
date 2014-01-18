package com.jayway.serviceregistry.boot;

import com.jayway.serviceregistry.messagebus.ServiceMessageReceiver;
import com.jayway.serviceregistry.messagebus.Topic;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
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

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Spring boot creates beans by default using the {@link org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration} class but we override the
 * {@link org.springframework.amqp.rabbit.connection.ConnectionFactory} here.
 */
@Configuration
class RabbitMQConfiguration {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfiguration.class);

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
    public AmqpAdmin amqpAdmin(CachingConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange labTopicExchange() {
        return new TopicExchange(Topic.getLabExchange());
    }

    @Bean
    public Queue clientQueue() {
        return amqpAdmin.declareQueue(); // Perhaps it should be durable if we need to redeploy this app during the lab?
    }

    /**
     * Binds to the service topic exchange
     */
    @Bean
    public Binding serviceTopicBinding() {
        return BindingBuilder.bind(clientQueue()).to(labTopicExchange()).with(Topic.SERVICE.getRoutingKey());
    }

    @Bean
    public CachingConnectionFactory rabbitConnectionFactory() {
        log.info("AMQP URL: {}", isBlank(amqpConnectionUri) ? "localhost:5672" : amqpConnectionUri);

        RabbitConnectionFactoryConfig config = uriToConnectionConfig(amqpConnectionUri);
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(config.getHost());
        connectionFactory.setPort(config.getPort());

        if (config.getUsername() != null) {
            connectionFactory.setUsername(config.getUsername());
        }
        if (config.getPassword() != null) {
            connectionFactory.setPassword(config.getPassword());
        }
        if (config.getVirtualHost() != null) {
            connectionFactory.setVirtualHost(StringUtils.remove(config.getVirtualHost(), '/'));
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
     * Hack to convert an AMQP URL to a RabbitConnectionFactoryConfig class
     */

    static RabbitConnectionFactoryConfig uriToConnectionConfig(String uri) {
        RabbitConnectionFactoryConfig properties = new RabbitConnectionFactoryConfig();
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
            String virtualHost = StringUtils.substringAfter(uri, hostWithPort + "/");


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

    /**
     * There's a bug in the {@link org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.RabbitConnectionFactoryProperties} class
     * which adds a "/" at the start of the virtual host. This fails when connecting to Cloud AMQP.
     */
    static class RabbitConnectionFactoryConfig {

        private String host = "localhost";

        private int port = 5672;

        private String username;

        private String password;

        private String virtualHost;

        public String getHost() {
            return this.host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return this.port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return this.username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getVirtualHost() {
            return this.virtualHost;
        }

        public void setVirtualHost(String virtualHost) {
            this.virtualHost = virtualHost;
        }

    }

    @PostConstruct
    void setMessageConverterToRabbitTemplate() {
        rabbitTemplate.setMessageConverter(messageConverter);
    }
}