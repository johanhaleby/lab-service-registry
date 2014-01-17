package com.jayway.serviceregistry.boot;

import org.junit.Test;

import static com.jayway.serviceregistry.boot.RabbitMQConfiguration.RabbitConnectionFactoryConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class AMQPUriToConnectionConfigTest {

    @Test public void
    parses_url_without_port_to_rabbit_connection_factory_properties() {
        // Given
        String uri = "amqp://user:pw@bunny.cloudamqp.com/virtual";

        // When
        RabbitConnectionFactoryConfig config = RabbitMQConfiguration.uriToConnectionConfig(uri);

        // Then
        assertThat(config.getHost()).isEqualTo("bunny.cloudamqp.com");
        assertThat(config.getPort()).isEqualTo(5672);
        assertThat(config.getUsername()).isEqualTo("user");
        assertThat(config.getPassword()).isEqualTo("pw");
        assertThat(config.getVirtualHost()).isEqualTo("virtual");
    }

    @Test public void
    parses_url_with_port_to_rabbit_connection_factory_properties() {
        // Given
        String uri = "amqp://user:pw@bunny.cloudamqp.com:4321/virtual";

        // When
        RabbitConnectionFactoryConfig config = RabbitMQConfiguration.uriToConnectionConfig(uri);

        // Then
        assertThat(config.getHost()).isEqualTo("bunny.cloudamqp.com");
        assertThat(config.getPort()).isEqualTo(4321);
        assertThat(config.getUsername()).isEqualTo("user");
        assertThat(config.getPassword()).isEqualTo("pw");
        assertThat(config.getVirtualHost()).isEqualTo("virtual");
    }

    @Test public void
    parses_url_without_virtual_host_and_port_to_rabbit_connection_factory_properties() {
        // Given
        String uri = "amqp://user:pw@bunny.cloudamqp.com";

        // When
        RabbitConnectionFactoryConfig config = RabbitMQConfiguration.uriToConnectionConfig(uri);

        // Then
        assertThat(config.getHost()).isEqualTo("bunny.cloudamqp.com");
        assertThat(config.getPort()).isEqualTo(5672);
        assertThat(config.getUsername()).isEqualTo("user");
        assertThat(config.getPassword()).isEqualTo("pw");
        assertThat(config.getVirtualHost()).isNull();
    }

    @Test public void
    parses_url_without_virtual_host_but_with_port_to_rabbit_connection_factory_properties() {
        // Given
        String uri = "amqp://user:pw@bunny.cloudamqp.com:4321";

        // When
        RabbitConnectionFactoryConfig config = RabbitMQConfiguration.uriToConnectionConfig(uri);

        // Then
        assertThat(config.getHost()).isEqualTo("bunny.cloudamqp.com");
        assertThat(config.getPort()).isEqualTo(4321);
        assertThat(config.getUsername()).isEqualTo("user");
        assertThat(config.getPassword()).isEqualTo("pw");
        assertThat(config.getVirtualHost()).isNull();
    }

    @Test public void
    uses_default_rabbit_connection_factory_properties_when_amqp_url_is_empty() {
            // Given
        String uri = "";

        // When
        RabbitConnectionFactoryConfig config = RabbitMQConfiguration.uriToConnectionConfig(uri);

        // Then
        assertThat(config.getHost()).isEqualTo("localhost");
        assertThat(config.getPort()).isEqualTo(5672);
        assertThat(config.getUsername()).isNull();
        assertThat(config.getPassword()).isNull();
        assertThat(config.getVirtualHost()).isNull();
    }
}
