package io.hoplin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionStringParserTest {

    @Test
    void base001()
    {
        final RabbitMQOptions options = ConnectionStringParser
                .parse("host=localhost;virtualHost=vhost1");

        assertEquals("localhost", options.getHost());
        assertEquals("vhost1", options.getVirtualHost());
    }

    @Test
    void base002()
    {
        final RabbitMQOptions options = ConnectionStringParser
                .parse("host=localhost;virtualHost=vhost1;username=greg;password=secret;product=My Product");

        assertEquals("localhost", options.getHost());
        assertEquals("vhost1", options.getVirtualHost());
        assertEquals("greg", options.getUser());
        assertEquals("My Product", options.getClientProperty("product"));


    }
}
